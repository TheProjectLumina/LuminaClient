package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v671.serializer.ResourcePackStackSerializer_v671;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackStackPacket;

public class ResourcePackStackSerializer_v898 extends ResourcePackStackSerializer_v671 {

    public static final ResourcePackStackSerializer_v898 INSTANCE = new ResourcePackStackSerializer_v898();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ResourcePackStackPacket packet) {
        buffer.writeBoolean(packet.isForcedToAccept());
        helper.writeArray(buffer, packet.getResourcePacks(), this::writeEntry);
        helper.writeString(buffer, packet.getGameVersion());
        helper.writeExperiments(buffer, packet.getExperiments());
        buffer.writeBoolean(packet.isExperimentsPreviouslyToggled());
        buffer.writeBoolean(packet.isHasEditorPacks());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ResourcePackStackPacket packet) {
        packet.setForcedToAccept(buffer.readBoolean());
        helper.readArray(buffer, packet.getResourcePacks(), this::readEntry);
        packet.setGameVersion(helper.readString(buffer));
        helper.readExperiments(buffer, packet.getExperiments());
        packet.setExperimentsPreviouslyToggled(buffer.readBoolean());
        packet.setHasEditorPacks(buffer.readBoolean());
    }
}
