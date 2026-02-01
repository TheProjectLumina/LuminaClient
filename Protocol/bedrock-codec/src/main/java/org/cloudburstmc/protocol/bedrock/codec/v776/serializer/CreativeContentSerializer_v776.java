package org.cloudburstmc.protocol.bedrock.codec.v776.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.codec.v407.serializer.CreativeContentSerializer_v407;
import org.cloudburstmc.protocol.bedrock.data.inventory.CreativeItemCategory;
import org.cloudburstmc.protocol.bedrock.data.inventory.CreativeItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.CreativeItemGroup;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.CreativeContentPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreativeContentSerializer_v776 extends CreativeContentSerializer_v407 {
    public static final CreativeContentSerializer_v776 INSTANCE = new CreativeContentSerializer_v776();

    protected static final CreativeItemCategory[] CATEGORIES = CreativeItemCategory.values();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, CreativeContentPacket packet) {
        helper.writeArray(buffer, packet.getGroups(), this::writeCreativeGroup);
        helper.writeArray(buffer, packet.getContents(), this::writeCreativeItem);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, CreativeContentPacket packet) {
        helper.readArray(buffer, packet.getGroups(), this::readCreativeGroup);
        helper.readArray(buffer, packet.getContents(), this::readCreativeItem);
    }

    protected CreativeItemGroup readCreativeGroup(ByteBuf buffer, BedrockCodecHelper helper) {
        CreativeItemCategory category = CATEGORIES[buffer.readIntLE()];
        String name = helper.readString(buffer);
        ItemData icon = helper.readItemInstance(buffer);
        return new CreativeItemGroup(category, name, icon);
    }

    protected void writeCreativeGroup(ByteBuf buffer, BedrockCodecHelper helper, CreativeItemGroup item) {
        buffer.writeIntLE(item.getCategory().ordinal());
        helper.writeString(buffer, item.getName());
        helper.writeItemInstance(buffer, item.getIcon());
    }

    @Override
    protected CreativeItemData readCreativeItem(ByteBuf buffer, BedrockCodecHelper helper) {
        int netId = VarInts.readUnsignedInt(buffer);
        ItemData item = helper.readItemInstance(buffer);
        int groupId = VarInts.readUnsignedInt(buffer);
        return new CreativeItemData(item, netId, groupId);
    }

    @Override
    protected void writeCreativeItem(ByteBuf buffer, BedrockCodecHelper helper, CreativeItemData item) {
        VarInts.writeUnsignedInt(buffer, item.getNetId());
        helper.writeItemInstance(buffer, item.getItem());
        VarInts.writeUnsignedInt(buffer, item.getGroupId());
    }
}
