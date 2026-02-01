package org.cloudburstmc.protocol.bedrock.codec.v407.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.inventory.CreativeItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.CreativeContentPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreativeContentSerializer_v407 implements BedrockPacketSerializer<CreativeContentPacket> {

    public static final CreativeContentSerializer_v407 INSTANCE = new CreativeContentSerializer_v407();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, CreativeContentPacket packet) {
        helper.writeArray(buffer, packet.getContents(), this::writeCreativeItem);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, CreativeContentPacket packet) {
        helper.readArray(buffer, packet.getContents(), this::readCreativeItem);
    }

    protected CreativeItemData readCreativeItem(ByteBuf buffer, BedrockCodecHelper helper) {
        int netId = VarInts.readUnsignedInt(buffer);
        ItemData item = helper.readItemInstance(buffer);
        return new CreativeItemData(item, netId, 0);
    }

    protected void writeCreativeItem(ByteBuf buffer, BedrockCodecHelper helper, CreativeItemData item) {
        VarInts.writeUnsignedInt(buffer, item.getNetId());
        helper.writeItemInstance(buffer, item.getItem());
    }
}
