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

package org.cloudburstmc.netty.handler.codec.raknet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;

public abstract class AdvancedChannelInboundHandler<T> extends ChannelInboundHandlerAdapter {
    private final TypeParameterMatcher matcher;

    public AdvancedChannelInboundHandler() {
        this.matcher = TypeParameterMatcher.find(this, AdvancedChannelInboundHandler.class, "T");
    }

    public AdvancedChannelInboundHandler(Class<? extends T> inboundMessageType) {
        this.matcher = TypeParameterMatcher.get(inboundMessageType);
    }

    protected boolean acceptInboundMessage(ChannelHandlerContext ctx, Object msg) throws Exception {
        return this.matcher.match(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean release = true;

        try {
            if (this.acceptInboundMessage(ctx, msg)) {
                this.channelRead0(ctx, (T) msg);
            } else {
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    protected abstract void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception;
}
