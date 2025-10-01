/*
 * Copyright 2024 CloudburstMC
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

package org.cloudburstmc.netty.handler.codec.raknet.server;

import org.cloudburstmc.netty.channel.raknet.RakServerChannel;
import org.cloudburstmc.netty.channel.raknet.config.RakServerMetrics;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class RakServerRateLimiter extends SimpleChannelInboundHandler<DatagramPacket> {
    public static final String NAME = "rak-server-rate-limiter";
    private static final InternalLogger log = InternalLoggerFactory.getInstance(RakServerRateLimiter.class);

    private final RakServerChannel channel;

    private final ConcurrentHashMap<InetAddress, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
    private final Map<InetAddress, Long> blockedConnections = new ConcurrentHashMap<>();

    private final Collection<InetAddress> exceptions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final AtomicLong globalCounter = new AtomicLong(0);

    private ScheduledFuture<?> tickFuture;
    private ScheduledFuture<?> blockedTickFuture;

    public RakServerRateLimiter(RakServerChannel channel) {
        this.channel = channel;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.tickFuture = ctx.channel().eventLoop().scheduleAtFixedRate(this::onRakTick, 10, 10, TimeUnit.MILLISECONDS);
        this.blockedTickFuture = ctx.channel().eventLoop().scheduleAtFixedRate(this::onBlockedTick, 100, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.tickFuture.cancel(false);
        this.blockedTickFuture.cancel(true);
        this.rateLimitMap.clear();
    }

    private void onRakTick() {
        this.rateLimitMap.clear();
        this.globalCounter.set(0);
    }

    private void onBlockedTick() {
        long currTime = System.currentTimeMillis();

        RakServerMetrics metrics = this.channel.config().getMetrics();

        Iterator<Map.Entry<InetAddress, Long>> iterator = this.blockedConnections.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<InetAddress, Long> entry = iterator.next();
            if (entry.getValue() != 0 && currTime > entry.getValue()) {
                iterator.remove();
                log.info("Unblocked address {}", entry.getKey());
                if (metrics != null) {
                    metrics.addressUnblocked(entry.getKey());
                }
            }
        }
    }

    public boolean blockAddress(InetAddress address, long time, TimeUnit unit) {
        if (this.exceptions.contains(address)) {
            return false;
        }

        long millis = unit.toMillis(time);
        this.blockedConnections.put(address, System.currentTimeMillis() + millis);

        if (this.channel.config().getMetrics() != null) {
            this.channel.config().getMetrics().addressBlocked(address);
        }
        return true;
    }

    public void unblockAddress(InetAddress address) {
        if (this.blockedConnections.remove(address) == null) {
            return;
        }

        log.info("Unblocked address {}", address);

        if (this.channel.config().getMetrics() != null) {
            this.channel.config().getMetrics().addressUnblocked(address);
        }
    }

    public boolean isAddressBlocked(InetAddress address) {
        return this.blockedConnections.containsKey(address);
    }

    public void addException(InetAddress address) {
        this.exceptions.add(address);
    }

    public void removeException(InetAddress address) {
        this.exceptions.remove(address);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagram) throws Exception {
        if (this.globalCounter.incrementAndGet() > this.channel.config().getGlobalPacketLimit()) {
            if (log.isTraceEnabled()) {
                log.trace("[{}] Dropped incoming packet because global packet limit was reached: {}", datagram.sender(), this.globalCounter.get());
            }
            return;
        }

        InetAddress address = datagram.sender().getAddress();
        if (this.blockedConnections.containsKey(address)) {
            return;
        }

        AtomicInteger counter = this.rateLimitMap.computeIfAbsent(address, a -> new AtomicInteger());
        if (counter.incrementAndGet() > this.channel.config().getPacketLimit() &&
                this.blockAddress(address, 10, TimeUnit.SECONDS)) {
            log.warn("[{}] Blocked because packet limit was reached", address);
        } else {
            ctx.fireChannelRead(datagram.retain());
        }
    }
}
