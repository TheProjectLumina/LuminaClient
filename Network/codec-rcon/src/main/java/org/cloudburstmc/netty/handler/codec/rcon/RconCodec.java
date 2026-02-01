package org.cloudburstmc.netty.handler.codec.rcon;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

@Sharable
public class RconCodec extends ByteToMessageCodec<RconMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RconMessage rconMessage, ByteBuf byteBuf) throws Exception {
        byteBuf.writeIntLE(rconMessage.getId());
        byteBuf.writeIntLE(rconMessage.getType());
        ByteBufUtil.writeAscii(byteBuf, rconMessage.getBody());
        // 2 null bytes
        byteBuf.writeByte(0);
        byteBuf.writeByte(0);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int id = byteBuf.readIntLE();
        int type = byteBuf.readIntLE();
        String body = readNullTerminatedString(byteBuf);

        // Discard remaining bytes
        byteBuf.readerIndex(byteBuf.writerIndex());

        list.add(new RconMessage(id, type, body));
    }

    private static String readNullTerminatedString(ByteBuf in) {
        StringBuilder read = new StringBuilder();
        byte readIn;
        while ((readIn = in.readByte()) != '\0') {
            read.append((char) readIn);
        }
        return read.toString();
    }
}
