/*
 * Copyright 2022 CloudburstMC
 *
 * CloudburstMC licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.cloudburstmc.netty.handler.codec.raknet.common;

import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_ACK;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_NACK;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_VALID;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTED_PING;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_DISCONNECTION_NOTIFICATION;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.MAXIMUM_ENCAPSULATED_HEADER_SIZE;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.RAKNET_DATAGRAM_HEADER_SIZE;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.SESSION_STALE_MS;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.UDP_HEADER_SIZE;

import org.cloudburstmc.netty.channel.raknet.RakChannel;
import org.cloudburstmc.netty.channel.raknet.RakDisconnectReason;
import org.cloudburstmc.netty.channel.raknet.RakPriority;
import org.cloudburstmc.netty.channel.raknet.RakReliability;
import org.cloudburstmc.netty.channel.raknet.RakSlidingWindow;
import org.cloudburstmc.netty.channel.raknet.RakState;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelMetrics;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.netty.channel.raknet.packet.EncapsulatedPacket;
import org.cloudburstmc.netty.channel.raknet.packet.RakDatagramPacket;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;
import org.cloudburstmc.netty.util.BitQueue;
import org.cloudburstmc.netty.util.FastBinaryMinHeap;
import org.cloudburstmc.netty.util.IntRange;
import org.cloudburstmc.netty.util.RakUtils;
import org.cloudburstmc.netty.util.RoundRobinArray;
import org.cloudburstmc.netty.util.SplitPacketHelper;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class RakSessionCodec extends ChannelDuplexHandler {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(RakSessionCodec.class);
    public static final String NAME = "rak-session-codec";

    private final RakChannel channel;
    private ScheduledFuture<?> tickFuture;

    private volatile RakState state;

    private volatile long lastTouched = System.currentTimeMillis();
    private volatile long lastFlush;

    // Reliability, Ordering, Sequencing and datagram indexes
    private RakSlidingWindow slidingWindow;
    private int splitIndex;
    private int datagramReadIndex;
    private int datagramWriteIndex;
    private int reliabilityReadIndex;
    private int reliabilityWriteIndex;
    private int[] orderReadIndex;
    private int[] orderWriteIndex;

    private RoundRobinArray<SplitPacketHelper> splitPackets;
    private BitQueue reliableDatagramQueue;

    private FastBinaryMinHeap<EncapsulatedPacket> outgoingPackets;
    private long[] outgoingPacketNextWeights;
    private FastBinaryMinHeap<EncapsulatedPacket>[] orderingHeaps;
    private long currentPingTime = -1;
    private long lastPingTime = -1;
    private long lastPongTime = -1;
    private Map<Integer, RakDatagramPacket> sentDatagrams;
    private Queue<IntRange> incomingAcks;
    private Queue<IntRange> incomingNaks;
    private Queue<IntRange> outgoingAcks;
    private Queue<IntRange> outgoingNaks;
    private long lastMinWeight;

    public RakSessionCodec(RakChannel channel) {
        this.channel = channel;
        this.setState(RakState.UNCONNECTED);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.setState(RakState.CONNECTED);
        int mtu = this.getMtu();

        this.slidingWindow = new RakSlidingWindow(mtu);

        this.outgoingPacketNextWeights = new long[4];
        this.initHeapWeights();

        int maxChannels = this.channel.config().getOption(RakChannelOption.RAK_ORDERING_CHANNELS);
        this.orderReadIndex = new int[maxChannels];
        this.orderWriteIndex = new int[maxChannels];

        // Noinspection unchecked
        this.orderingHeaps = new FastBinaryMinHeap[maxChannels];
        for (int i = 0; i < maxChannels; i++) {
            orderingHeaps[i] = new FastBinaryMinHeap<>(64);
        }

        this.outgoingPackets = new FastBinaryMinHeap<>(8);
        this.sentDatagrams = new ConcurrentHashMap<>();

        this.incomingAcks = new ArrayDeque<>();
        this.incomingNaks = new ArrayDeque<>();
        this.outgoingAcks = new ArrayDeque<>();
        this.outgoingNaks = new ArrayDeque<>();

        this.reliableDatagramQueue = new BitQueue(512);
        this.splitPackets = new RoundRobinArray<>(256);

        // After session is fully initialized, start ticking.
        boolean autoFlush = this.channel.config().isAutoFlush();
        // Make sure there happens at least one flush per 10ms to respect standard RakNet behavior
        int flushInterval = autoFlush ? this.channel.config().getFlushInterval() : 10;
        this.tickFuture = ctx.channel().eventLoop().scheduleAtFixedRate(this::tryTick, 0, flushInterval, TimeUnit.MILLISECONDS);

        ctx.fireChannelActive(); // fire channel active on rakPipeline()
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (this.state == RakState.DISCONNECTED && this.tickFuture == null) {
            // Already deinitialized
            return;
        }
        this.setState(RakState.DISCONNECTED);
        this.tickFuture.cancel(false);
        this.tickFuture = null;

        // Perform resource clean up.
        for (SplitPacketHelper helper : this.splitPackets) {
            if (helper != null) {
                helper.release();
            }
        }
        this.splitPackets = null;

        for (RakDatagramPacket packet : this.sentDatagrams.values()) {
            packet.release();
        }
        this.sentDatagrams = null;

        FastBinaryMinHeap<EncapsulatedPacket>[] orderingHeaps = this.orderingHeaps;
        this.orderingHeaps = null;
        if (orderingHeaps != null) {
            for (FastBinaryMinHeap<EncapsulatedPacket> orderingHeap : orderingHeaps) {
                EncapsulatedPacket packet;
                while ((packet = orderingHeap.poll()) != null) {
                    packet.release();
                }
                orderingHeap.release();
            }
        }

        FastBinaryMinHeap<EncapsulatedPacket> outgoingPackets = this.outgoingPackets;
        this.outgoingPackets = null;
        if (outgoingPackets != null) {
            EncapsulatedPacket packet;
            while ((packet = outgoingPackets.poll()) != null) {
                packet.release();
            }
            outgoingPackets.release();
        }

        if (log.isTraceEnabled()) {
            log.trace("RakNet Session ({} => {}) closed!", this.channel.localAddress(), this.getRemoteAddress());
        }
    }

    private void initHeapWeights() {
        for (int priorityLevel = 0; priorityLevel < 4; priorityLevel++) {
            this.outgoingPacketNextWeights[priorityLevel] = (1 << priorityLevel) * priorityLevel + priorityLevel;
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof ByteBuf) {
            msg = new RakMessage((ByteBuf) msg);
        } else if (!(msg instanceof RakMessage)) {
            throw new IllegalArgumentException("Message must be a ByteBuf or RakMessage");
        }

        try {
            this.send(ctx, (RakMessage) msg);
            promise.setSuccess(null);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (!this.channel.config().isAutoFlush()) {
            this.internalFlush(ctx);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (!(msg instanceof RakDatagramPacket)) {
                // We don't want to let anything through that isn't RakNet related.
                return;
            }
            RakDatagramPacket packet = (RakDatagramPacket) msg;
            if (this.state == RakState.UNCONNECTED) {
                log.debug("{} received message from inactive channel: {}", this.getRemoteAddress(), packet);
            } else {
                this.handleDatagram(ctx, packet);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.disconnect0(RakDisconnectReason.DISCONNECTED).addListener(future -> {
            if (future.cause() == null) {
                promise.trySuccess();
            } else {
                promise.tryFailure(future.cause());
            }
        });
    }

    private void send(ChannelHandlerContext ctx, RakMessage message) {
        if (this.state == RakState.UNCONNECTED) {
            throw new IllegalStateException("Can not send RakMessage to inactive channel");
        }

        if (message.content().getUnsignedByte(message.content().readerIndex()) == 0xc0) {
            throw new IllegalArgumentException();
        }

        RakChannelMetrics metrics = this.getMetrics();
        if (metrics != null) {
            metrics.encapsulatedOut(1);
        }
        EncapsulatedPacket[] packets = this.createEncapsulated(message);
        if (message.priority() == RakPriority.IMMEDIATE) {
            this.sendImmediate(ctx, packets);
            return;
        }

        long weight = this.getNextWeight(message.priority());
        if (packets.length == 1) {
            this.outgoingPackets.insert(weight, packets[0]);
        } else {
            this.outgoingPackets.insertSeries(weight, packets);
        }
    }

    private void handleDatagram(ChannelHandlerContext ctx, RakDatagramPacket packet) {
        this.touch();
        RakChannelMetrics metrics = this.getMetrics();
        if (metrics != null) {
            metrics.rakDatagramsIn(1);
        }

        this.slidingWindow.onPacketReceived(packet.getSendTime());

        int prevSequenceIndex = this.datagramReadIndex;
        if (prevSequenceIndex <= packet.getSequenceIndex()) {
            this.datagramReadIndex = packet.getSequenceIndex() + 1;
        }

        int missedDatagrams = packet.getSequenceIndex() - prevSequenceIndex;
        if (missedDatagrams > 0) {
            this.outgoingNaks.offer(new IntRange(packet.getSequenceIndex() - missedDatagrams, packet.getSequenceIndex() - 1));
        }

        this.outgoingAcks.offer(new IntRange(packet.getSequenceIndex(), packet.getSequenceIndex()));

        for (final EncapsulatedPacket encapsulated : packet.getPackets()) {
            if (encapsulated.getReliability().isReliable()) {
                int missed = encapsulated.getReliabilityIndex() - this.reliabilityReadIndex;
                if (missed > 0) {
                    if (missed < this.reliableDatagramQueue.size()) {
                        if (this.reliableDatagramQueue.get(missed)) {
                            this.reliableDatagramQueue.set(missed, false);
                        } else {
                            // Duplicate packet
                            continue;
                        }
                    } else {
                        int count = (missed - this.reliableDatagramQueue.size());
                        for (int i = 0; i < count; i++) {
                            this.reliableDatagramQueue.add(true);
                        }

                        this.reliableDatagramQueue.add(false);
                    }
                } else if (missed == 0) {
                    this.reliabilityReadIndex++;
                    if (!this.reliableDatagramQueue.isEmpty()) {
                        this.reliableDatagramQueue.poll();
                    }
                } else {
                    // Duplicate packet
                    continue;
                }

                while (!this.reliableDatagramQueue.isEmpty() && !this.reliableDatagramQueue.peek()) {
                    this.reliableDatagramQueue.poll();
                    ++this.reliabilityReadIndex;
                }
            }

            if (encapsulated.isSplit()) {
                final EncapsulatedPacket reassembled = this.getReassembledPacket(encapsulated, ctx.alloc());
                if (reassembled == null) {
                    // Not reassembled
                    continue;
                }
                if (metrics != null) {
                    metrics.encapsulatedIn(1);
                }
                try {
                    this.checkForOrdered(ctx, reassembled);
                } finally {
                    reassembled.release();
                }
            } else {
                if (metrics != null) {
                    metrics.encapsulatedIn(1);
                }
                this.checkForOrdered(ctx, encapsulated);
            }
        }
    }

    private void checkForOrdered(ChannelHandlerContext ctx, EncapsulatedPacket packet) {
        if (packet.getReliability().isOrdered()) {
            this.onOrderedReceived(ctx, packet);
        } else {
            ctx.fireChannelRead(packet.retain());
        }
    }

    private void onOrderedReceived(ChannelHandlerContext ctx, EncapsulatedPacket packet) {
        FastBinaryMinHeap<EncapsulatedPacket> binaryHeap = this.orderingHeaps[packet.getOrderingChannel()];
        if (this.orderReadIndex[packet.getOrderingChannel()] < packet.getOrderingIndex()) {
            // Not next in line so add to queue.
            binaryHeap.insert(packet.getOrderingIndex(), packet.retain());
            return;
        } else if (this.orderReadIndex[packet.getOrderingChannel()] > packet.getOrderingIndex()) {
            // We already have this
            return;
        }
        this.orderReadIndex[packet.getOrderingChannel()]++;

        // Can be handled
        ctx.fireChannelRead(packet.retain());

        EncapsulatedPacket queuedPacket;
        while ((queuedPacket = binaryHeap.peek()) != null) {
            if (queuedPacket.getOrderingIndex() == this.orderReadIndex[packet.getOrderingChannel()]) {
                try {
                    // We got the expected packet
                    binaryHeap.remove();
                    this.orderReadIndex[packet.getOrderingChannel()]++;
                    ctx.fireChannelRead(queuedPacket.retain());
                } finally {
                    queuedPacket.release();
                }
            } else {
                // Found a gap. Wait till we start receive another ordered packet.
                break;
            }
        }
    }

    private EncapsulatedPacket getReassembledPacket(EncapsulatedPacket splitPacket, ByteBufAllocator alloc) {
        this.checkForClosed();

        SplitPacketHelper helper = this.splitPackets.get(splitPacket.getPartId());
        if (helper == null) {
            this.splitPackets.set(splitPacket.getPartId(), helper = new SplitPacketHelper(splitPacket.getPartCount()));
        }

        // Try reassembling the packet.
        EncapsulatedPacket result = helper.add(splitPacket, alloc);
        if (result != null) {
            // Packet reassembled. Remove the helper
            this.splitPackets.remove(splitPacket.getPartId(), helper);
        }

        return result;
    }

    private void tryTick() {
        try {
            this.onTick();
        } catch (Throwable t) {
            log.error("[{}] Error while ticking RakSessionCodec state={} channelActive={}", this.getRemoteAddress(), this.state, this.channel.isActive(), t);
            this.channel.close();
        }
    }

    private void onTick() {
        long curTime = System.currentTimeMillis();

        int maxQueuedBytes = this.channel.config().getOption(RakChannelOption.RAK_MAX_QUEUED_BYTES);

        if (maxQueuedBytes > 0) {
            int queuedBytes = 0;
            try {
                for (EncapsulatedPacket packet : this.outgoingPackets) {
                    queuedBytes += packet.getBuffer().readableBytes();
                    if (queuedBytes > maxQueuedBytes) {
                        this.disconnect(RakDisconnectReason.QUEUE_TOO_LONG);
                        return;
                    }
                }
            } finally {
                RakChannelMetrics metrics = this.getMetrics();
                if (metrics != null) {
                    metrics.queuedPacketBytes(queuedBytes);
                }
            }
        }

        if (this.state == RakState.UNCONNECTED) {
            if (this.isTimedOut(curTime)) {
                this.close(RakDisconnectReason.TIMED_OUT);
            }
            return;
        }

        if (this.isTimedOut(curTime)) {
            this.disconnect(RakDisconnectReason.TIMED_OUT);
            return;
        }

        ChannelHandlerContext ctx = ctx();

        if (this.currentPingTime + 2000L < curTime) {
            ByteBuf buffer = ctx.alloc().ioBuffer(9);
            buffer.writeByte(ID_CONNECTED_PING);
            buffer.writeLong(curTime);
            this.currentPingTime = curTime;
            this.write(ctx, new RakMessage(buffer, RakReliability.UNRELIABLE, RakPriority.IMMEDIATE), ctx.voidPromise());
        }

         this.internalFlush(ctx);
    }

    private void internalFlush(ChannelHandlerContext ctx) {
        long curTime = System.currentTimeMillis();
        if (this.lastFlush == curTime) {
            return; // do not flush multiple times within one ms
        }
        this.lastFlush = curTime;

        this.handleIncomingAcknowledge(ctx, curTime, this.incomingAcks, false);
        this.handleIncomingAcknowledge(ctx, curTime, this.incomingNaks, true);

        // Send our know outgoing acknowledge packets.
        int mtuSize = this.getMtu();
        int ackMtu = mtuSize - RAKNET_DATAGRAM_HEADER_SIZE;
        int writtenAcks = 0;
        int writtenNacks = 0;

        // if (this.slidingWindow.shouldSendAcks(curTime)) {
        while (!this.outgoingAcks.isEmpty()) {
            ByteBuf buffer = ctx.alloc().ioBuffer(ackMtu);
            buffer.writeByte(FLAG_VALID | FLAG_ACK);
            writtenAcks += RakUtils.writeAckEntries(buffer, this.outgoingAcks, ackMtu - 1);
            ctx.write(buffer);
            this.slidingWindow.onSendAck();
        }
        // }

        while (!this.outgoingNaks.isEmpty()) {
            ByteBuf buffer = ctx.alloc().ioBuffer(ackMtu);
            buffer.writeByte(FLAG_VALID | FLAG_NACK);
            writtenNacks += RakUtils.writeAckEntries(buffer, this.outgoingNaks, ackMtu - 1);
            ctx.write(buffer);
        }

        // Send packets that are stale first
        int resendCount = this.sendStaleDatagrams(ctx, curTime);
        // Now send usual packets
        this.sendDatagrams(ctx, curTime, mtuSize);
        // Finally flush channel
        ctx.flush();

        RakChannelMetrics metrics = this.getMetrics();
        if (metrics != null) {
            metrics.nackOut(writtenNacks);
            metrics.ackOut(writtenAcks);
            metrics.rakStaleDatagrams(resendCount);
        }
    }

    private void handleIncomingAcknowledge(ChannelHandlerContext ctx, long curTime, Queue<IntRange> queue, boolean nack) {
        if (queue.isEmpty()) {
            return;
        }

//        if (nack) {
//            this.slidingWindow.onNak();
//        }

        IntRange range;
        while ((range = queue.poll()) != null) {
            for (int i = range.start; i <= range.end; i++) {
                RakDatagramPacket datagram = this.sentDatagrams.remove(i);
                if (datagram != null) {
                    if (nack) {
                        this.onIncomingNack(ctx, datagram, curTime);
                    } else {
                        this.onIncomingAck(datagram, curTime);
                    }
                }
            }
        }
    }

    private void onIncomingAck(RakDatagramPacket datagram, long curTime) {
        try {
            this.slidingWindow.onAck(curTime, datagram, this.datagramReadIndex);
        } finally {
            datagram.release();
        }
    }

    private void onIncomingNack(ChannelHandlerContext ctx, RakDatagramPacket datagram, long curTime) {
        if (log.isTraceEnabled()) {
            log.trace("NAK'ed datagram {} from {}", datagram.getSequenceIndex(), this.getRemoteAddress());
        }

        this.slidingWindow.onNak(); // TODO: verify this
        this.sendDatagram(ctx, datagram, curTime);
    }

    private int sendStaleDatagrams(ChannelHandlerContext ctx, long curTime) {
        if (this.sentDatagrams.isEmpty()) {
            return 0;
        }

        boolean hasResent = false;
        int resendCount = 0;
        int transmissionBandwidth = this.slidingWindow.getRetransmissionBandwidth();

        for (RakDatagramPacket datagram : this.sentDatagrams.values()) {
            if (datagram.getNextSend() <= curTime) {
                int size = datagram.getSize();
                if (transmissionBandwidth < size) {
                    break;
                }
                transmissionBandwidth -= size;

                if (!hasResent) {
                    hasResent = true;
                }
                if (log.isTraceEnabled()) {
                    log.trace("Stale datagram {} from {}", datagram.getSequenceIndex(), this.getRemoteAddress());
                }
                resendCount++;
                this.sendDatagram(ctx, datagram, curTime);
            }
        }

        if (hasResent) {
            this.slidingWindow.onResend(curTime);
        }

        return resendCount;
    }

    private void sendDatagrams(ChannelHandlerContext ctx, long curTime, int mtuSize) {
        if (this.outgoingPackets.isEmpty()) {
            return;
        }

        int transmissionBandwidth = this.slidingWindow.getTransmissionBandwidth();
        RakDatagramPacket datagram = RakDatagramPacket.newInstance();
        datagram.setSendTime(curTime);
        EncapsulatedPacket packet;

        while ((packet = this.outgoingPackets.peek()) != null) {
            int size = packet.getSize();
            if (transmissionBandwidth < size) {
                break;
            }

            transmissionBandwidth -= size;
            this.outgoingPackets.remove();

            // Send full datagram
            if (!datagram.tryAddPacket(packet, mtuSize)) {
                this.sendDatagram(ctx, datagram, curTime);

                datagram = RakDatagramPacket.newInstance();
                datagram.setSendTime(curTime);
                if (!datagram.tryAddPacket(packet, mtuSize)) {
                    throw new IllegalArgumentException("Packet too large to fit in MTU (size: " + packet.getSize() + ", MTU: " + mtuSize + ")");
                }
            }
        }

        if (!datagram.getPackets().isEmpty()) {
            this.sendDatagram(ctx, datagram, curTime);
        }
    }

    private void sendImmediate(ChannelHandlerContext ctx, EncapsulatedPacket[] packets) {
        long curTime = System.currentTimeMillis();
        for (EncapsulatedPacket packet : packets) {
            RakDatagramPacket datagram = RakDatagramPacket.newInstance();
            datagram.setSendTime(curTime);
            if (!datagram.tryAddPacket(packet, this.getMtu())) {
                throw new IllegalArgumentException("Packet too large to fit in MTU (size: " + packet.getSize() + ", MTU: " + this.getMtu() + ")");
            }
            this.sendDatagram(ctx, datagram, curTime);
        }
        ctx.flush();
    }

    private void sendDatagram(ChannelHandlerContext ctx, RakDatagramPacket datagram, long time) {
        if (!this.channel.parent().eventLoop().inEventLoop()) {
            // Make sure this runs on correct thread
            log.error("Tried to send datagrams from wrong thread: {}", Thread.currentThread().getName(), new Throwable());
            this.channel.parent().eventLoop().execute(() -> this.sendDatagram(ctx, datagram, time));
            return;
        }

        if (datagram.getPackets().isEmpty()) {
            throw new IllegalArgumentException("RakNetDatagram with no packets");
        }

        RakChannelMetrics metrics = this.getMetrics();
        if (metrics != null) {
            metrics.rakDatagramsOut(1);
        }

        int oldIndex = datagram.getSequenceIndex();
        datagram.setSequenceIndex(this.datagramWriteIndex++);

        for (EncapsulatedPacket packet : datagram.getPackets()) {
            // Check if packet is reliable so it can be resent later if a NAK is received.
            if (packet.getReliability().isReliable()) {
                datagram.setNextSend(time + this.slidingWindow.getRtoForRetransmission());
                if (oldIndex == -1) {
                    this.slidingWindow.onReliableSend(datagram);
                } else {
                    this.sentDatagrams.remove(oldIndex, datagram);
                }
                this.sentDatagrams.put(datagram.getSequenceIndex(), datagram.retain()); // Keep for resending
                break;
            }
        }
        ctx.write(datagram);
    }

    private ChannelHandlerContext ctx() {
        return this.channel.rakPipeline().context(RakSessionCodec.NAME);
    }

    private EncapsulatedPacket[] createEncapsulated(RakMessage rakMessage) {
        int maxLength = this.getMtu() - MAXIMUM_ENCAPSULATED_HEADER_SIZE - RAKNET_DATAGRAM_HEADER_SIZE;

        ByteBuf[] buffers;
        int splitId = 0;
        RakReliability reliability = rakMessage.reliability();
        ByteBuf buffer = rakMessage.content();
        int orderingChannel = rakMessage.channel();

        if (buffer.readableBytes() > maxLength) {
            // Packet requires splitting
            // Adjust reliability
            switch (reliability) {
                case UNRELIABLE:
                    reliability = RakReliability.RELIABLE;
                    break;
                case UNRELIABLE_SEQUENCED:
                    reliability = RakReliability.RELIABLE_SEQUENCED;
                    break;
                case UNRELIABLE_WITH_ACK_RECEIPT:
                    reliability = RakReliability.RELIABLE_WITH_ACK_RECEIPT;
                    break;
            }

            int split = ((buffer.readableBytes() - 1) / maxLength) + 1;
            buffer.retain(split);

            buffers = new ByteBuf[split];
            for (int i = 0; i < split; i++) {
                buffers[i] = buffer.readSlice(Math.min(maxLength, buffer.readableBytes()));
            }
            if (buffer.isReadable()) {
                throw new IllegalStateException("Buffer still has bytes to read!");
            }

            // Allocate split ID
            splitId = this.splitIndex++;
        } else {
            buffers = new ByteBuf[]{buffer.readRetainedSlice(buffer.readableBytes())};
        }

        // Set meta
        // TODO: sequencing
        int orderingIndex = 0;
        if (reliability.isOrdered()) {
            orderingIndex = this.orderWriteIndex[orderingChannel]++;
        }

        // Now create the packets.
        EncapsulatedPacket[] packets = new EncapsulatedPacket[buffers.length];
        for (int i = 0, parts = buffers.length; i < parts; i++) {
            EncapsulatedPacket packet = EncapsulatedPacket.newInstance();
            packet.setBuffer(buffers[i]);
            packet.setNeedsBAS(true);
            packet.setOrderingChannel((short) orderingChannel);
            packet.setOrderingIndex(orderingIndex);
            // packet.setSequenceIndex(sequencingIndex);
            packet.setReliability(reliability);
            if (reliability.isReliable()) {
                packet.setReliabilityIndex(this.reliabilityWriteIndex++);
            }

            if (parts > 1) {
                packet.setSplit(true);
                packet.setPartIndex(i);
                packet.setPartCount(parts);
                packet.setPartId(splitId);
            }

            packets[i] = packet;
        }
        return packets;
    }

    private long getNextWeight(RakPriority priority) {
        int priorityLevel = priority.ordinal();
        long next = this.outgoingPacketNextWeights[priorityLevel];

        if (!this.outgoingPackets.isEmpty()) {
            if (next >= this.lastMinWeight) {
                next = this.lastMinWeight + (1L << priorityLevel) * priorityLevel + priorityLevel;
                this.outgoingPacketNextWeights[priorityLevel] = next + (1L << priorityLevel) * (priorityLevel + 1) + priorityLevel;
            }
        } else {
            this.initHeapWeights();
        }
        this.lastMinWeight = next - (1L << priorityLevel) * priorityLevel + priorityLevel;
        return next;
    }

    public void disconnect() {
        this.disconnect(RakDisconnectReason.DISCONNECTED);
    }

    public void disconnect(RakDisconnectReason reason) {
        // Ensure we disconnect on the right thread
        if (this.channel.parent().eventLoop().inEventLoop()) {
            this.disconnect0(reason);
        } else {
            this.channel.parent().eventLoop().execute(() -> this.disconnect0(reason));
        }
    }

    private ChannelPromise disconnect0(RakDisconnectReason reason) {
        if (this.state == RakState.UNCONNECTED || this.state == RakState.DISCONNECTING) {
            return this.channel.voidPromise();
        }
        this.setState(RakState.DISCONNECTING);

        if (log.isDebugEnabled()) {
            log.debug("Disconnecting RakNet Session ({} => {}) due to {}", this.channel.localAddress(), this.getRemoteAddress(), reason);
        }

        ChannelHandlerContext ctx = this.ctx();

        ByteBuf buffer = ctx.alloc().ioBuffer(1);
        buffer.writeByte(ID_DISCONNECTION_NOTIFICATION);
        RakMessage rakMessage = new RakMessage(buffer, RakReliability.RELIABLE, RakPriority.IMMEDIATE);

        ChannelPromise promise = ctx.newPromise();
        promise.addListener((ChannelFuture future) -> // The channel provided in ChannelFuture is parent channel,
                this.channel.pipeline().fireUserEventTriggered(reason).close()); // but we want RakChannel instead
        this.write(ctx, rakMessage, promise);
        return promise;
    }

    public void close(RakDisconnectReason reason) {
        if (this.state == RakState.DISCONNECTING) {
            return;
        }
        this.setState(RakState.DISCONNECTING);

        if (log.isDebugEnabled()) {
            log.debug("Closing RakNet Session ({} => {}) due to {}", this.channel.localAddress(), this.getRemoteAddress(), reason);
        }

        this.channel.pipeline().fireUserEventTriggered(reason).close();
    }

    public boolean isClosed() {
        return this.state == RakState.UNCONNECTED;
    }

    private void checkForClosed() {
        if (this.state == RakState.UNCONNECTED) {
            throw new IllegalStateException("RakSession is closed!");
        }
    }

    private void setState(RakState state) {
        if (this.state == state) {
            return;
        }
        this.state = state;

        RakChannelMetrics metrics = this.getMetrics();
        if (metrics != null) {
            metrics.stateChange(state);
        }
    }

    public void recalculatePongTime(long pingTime) {
        if (this.currentPingTime == pingTime) {
            this.lastPingTime = this.currentPingTime;
            this.lastPongTime = System.currentTimeMillis();
        }
    }

    private void touch() {
        this.checkForClosed();
        this.lastTouched = System.currentTimeMillis();
    }

    public boolean isStale(long curTime) {
        return curTime - this.lastTouched >= SESSION_STALE_MS;
    }

    public boolean isStale() {
        return this.isStale(System.currentTimeMillis());
    }

    public boolean isTimedOut(long curTime) {
        return curTime - this.lastTouched >= this.channel.config().getOption(RakChannelOption.RAK_SESSION_TIMEOUT);
    }

    public boolean isTimedOut() {
        return this.isTimedOut(System.currentTimeMillis());
    }

    public long getPing() {
        return this.lastPongTime - this.lastPingTime;
    }

    public double getRTT() {
        return this.slidingWindow.getRTT();
    }

    public int getMtu() {
        return this.channel.config().getMtu() - UDP_HEADER_SIZE - (this.getRemoteAddress().getAddress() instanceof Inet6Address ? 40 : 20);
    }

    public RakChannelMetrics getMetrics() {
        return this.channel.config().getMetrics();
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) this.channel.remoteAddress();
    }

    protected Queue<IntRange> getAcknowledgeQueue(boolean nack) {
        return nack ? this.incomingNaks : this.incomingAcks;
    }

    public Channel getChannel() {
        return channel;
    }
}
