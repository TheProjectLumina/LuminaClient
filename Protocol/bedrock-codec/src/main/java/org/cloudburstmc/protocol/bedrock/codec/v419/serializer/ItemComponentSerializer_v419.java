package org.cloudburstmc.protocol.bedrock.codec.v419.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemVersion;
import org.cloudburstmc.protocol.bedrock.packet.ItemComponentPacket;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemComponentSerializer_v419 implements BedrockPacketSerializer<ItemComponentPacket> {

    public static final ItemComponentSerializer_v419 INSTANCE = new ItemComponentSerializer_v419();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ItemComponentPacket packet) {
        helper.writeArray(buffer, packet.getItems(), (buf, packetHelper, item) -> {
            packetHelper.writeString(buf, item.getIdentifier());
            packetHelper.writeTag(buf, item.getComponentData());
        });
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ItemComponentPacket packet) {
        helper.readArray(buffer, packet.getItems(), (buf, packetHelper) -> {
            String name = packetHelper.readString(buf);
            NbtMap data = packetHelper.readTag(buf, NbtMap.class);
            return new SimpleItemDefinition(name, 0, ItemVersion.LEGACY, true, data);
        });
    }
}
