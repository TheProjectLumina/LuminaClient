package org.cloudburstmc.netty.handler.codec.rcon.handler;

import org.cloudburstmc.netty.handler.codec.rcon.RconEventListener;
import org.cloudburstmc.netty.handler.codec.rcon.RconMessage;

import java.security.MessageDigest;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class RconHandler extends SimpleChannelInboundHandler<RconMessage> {
    private final RconEventListener eventListener;
    private final byte[] password;
    private boolean authed = false;

    public RconHandler(RconEventListener eventListener, byte[] password) {
        this.eventListener = eventListener;
        this.password = password;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RconMessage rconMessage) throws Exception {
        if (!authed) {
            if (rconMessage.getType() != RconMessage.AUTH) {
                return;
            }

            byte[] sentPassword = rconMessage.getBody().getBytes(CharsetUtil.UTF_8);

            ctx.channel().writeAndFlush(new RconMessage(rconMessage.getId(), RconMessage.RESPONSE_VALUE, ""));

            if (MessageDigest.isEqual(password, sentPassword)) {
                authed = true;
                ctx.channel().writeAndFlush(new RconMessage(rconMessage.getId(), RconMessage.AUTH_RESPONSE, ""));
            } else {
                ctx.channel().writeAndFlush(new RconMessage(-1, RconMessage.AUTH_RESPONSE, ""));
            }
        } else if (rconMessage.getType() == RconMessage.EXECCOMMAND) {
            Channel channel = ctx.channel();

            String output = eventListener.onMessage(rconMessage.getBody());
            channel.writeAndFlush(new RconMessage(rconMessage.getId(), RconMessage.RESPONSE_VALUE, output), ctx.voidPromise());
        }
    }
}
