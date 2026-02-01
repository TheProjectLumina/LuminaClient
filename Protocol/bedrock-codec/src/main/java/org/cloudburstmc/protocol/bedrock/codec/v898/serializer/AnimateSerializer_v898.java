package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v859.serializer.AnimateSerializer_v859;
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import static org.cloudburstmc.protocol.bedrock.packet.AnimatePacket.Action;

public class AnimateSerializer_v898 extends AnimateSerializer_v859 {

    public static final AnimateSerializer_v898 INSTANCE = new AnimateSerializer_v898();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, AnimatePacket packet) {
        Action action = packet.getAction();
        buffer.writeByte(types.get(action));
        VarInts.writeUnsignedLong(buffer, packet.getRuntimeEntityId());
        buffer.writeFloatLE(packet.getData());
        helper.writeOptional(buffer, (source) -> source != AnimatePacket.SwingSource.NONE, packet.getSwingSource(), (buf, source) -> helper.writeString(buf, source.getName()));
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, AnimatePacket packet) {
        Action action = types.get(buffer.readByte());
        packet.setAction(action);
        packet.setRuntimeEntityId(VarInts.readUnsignedLong(buffer));
        packet.setData(buffer.readFloatLE());
        packet.setSwingSource(helper.readOptional(buffer, AnimatePacket.SwingSource.NONE, (buf, h) -> AnimatePacket.SwingSource.from(h.readString(buf))));
    }
}
