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

package org.cloudburstmc.netty.channel.raknet;

import org.cloudburstmc.netty.channel.raknet.packet.EncapsulatedPacket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class RakChannelPipeline extends DefaultChannelPipeline {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(RakChannelPipeline.class);

    private final RakChildChannel child;

    protected RakChannelPipeline(Channel parent, RakChildChannel child) {
        super(parent);
        this.child = child;
    }

    @Override
    protected void onUnhandledInboundChannelActive() {
    }

    @Override
    protected void onUnhandledInboundChannelInactive() {
        if (this.child.isActive()) {
            this.child.setActive(false);
            this.child.pipeline().fireChannelInactive();
        }
    }

    @Override
    protected void onUnhandledInboundMessage(ChannelHandlerContext ctx, Object msg) {
        try {
            final Object message = msg instanceof EncapsulatedPacket ? ((EncapsulatedPacket) msg).toMessage() : msg;
            ReferenceCountUtil.retain(message);
            if (this.child.eventLoop().inEventLoop()) {
                this.child.pipeline().fireChannelRead(message).fireChannelReadComplete();
            } else {
                this.child.eventLoop().execute(() -> {
                    this.child.pipeline()
                            .fireChannelRead(message)
                            .fireChannelReadComplete();
                });
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    protected void onUnhandledInboundUserEventTriggered(Object evt) {
        this.child.pipeline().fireUserEventTriggered(evt);
        if (evt instanceof RakDisconnectReason) {
            this.child.close();
        }
    }

    @Override
    protected void onUnhandledInboundException(Throwable cause) {
        log.error("Exception thrown in RakNet pipeline", cause);
    }
}
