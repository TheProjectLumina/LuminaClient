package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v748.serializer.MobEffectSerializer_v748;
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket;

public class MobEffectSerializer_v898 extends MobEffectSerializer_v748 {

    public static final MobEffectSerializer_v898 INSTANCE = new MobEffectSerializer_v898();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, MobEffectPacket packet) {
        super.serialize(buffer, helper, packet);
        buffer.writeBoolean(packet.isAmbient());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, MobEffectPacket packet) {
        super.deserialize(buffer, helper, packet);
        packet.setAmbient(buffer.readBoolean());
    }
}
