package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDataStorePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerboundDataStoreSerializer_v898 implements BedrockPacketSerializer<ServerboundDataStorePacket> {

    public static final ServerboundDataStoreSerializer_v898 INSTANCE = new ServerboundDataStoreSerializer_v898();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDataStorePacket packet) {
        helper.writeString(buffer, packet.getDataStoreName());
        helper.writeString(buffer, packet.getProperty());
        helper.writeString(buffer, packet.getPath());

        Object value = packet.getData();

        int type = value instanceof Double ? 0 : value instanceof Boolean ? 1 : value instanceof String ? 2 : -1;

        VarInts.writeUnsignedInt(buffer, type);

        switch (type) {
            case 0:
                buffer.writeDoubleLE((double) value);
                break;
            case 1:
                buffer.writeBoolean((boolean) value);
                break;
            case 2:
                helper.writeString(buffer, (String) value);
                break;
            default:
                throw new IllegalStateException("Invalid data store data type");
        }

        buffer.writeIntLE(packet.getUpdateCount());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDataStorePacket packet) {
        packet.setDataStoreName(helper.readString(buffer));
        packet.setProperty(helper.readString(buffer));
        packet.setPath(helper.readString(buffer));

        int type = VarInts.readUnsignedInt(buffer);

        switch (type) {
            case 0:
                packet.setData(buffer.readDoubleLE());
                break;
            case 1:
                packet.setData(buffer.readBoolean());
                break;
            case 2:
                packet.setData(helper.readString(buffer));
                break;
            default:
                throw new IllegalStateException("Invalid data store data type: " + type);
        }

        packet.setUpdateCount((int) buffer.readUnsignedIntLE());
    }
}

