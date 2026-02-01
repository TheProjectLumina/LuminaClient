package org.cloudburstmc.protocol.bedrock.codec.v786.serializer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.PlayerVideoCapturePacket;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerVideoCaptureSerializer_v786 implements BedrockPacketSerializer<PlayerVideoCapturePacket> {
    public static final PlayerVideoCaptureSerializer_v786 INSTANCE = new PlayerVideoCaptureSerializer_v786();

    private static final PlayerVideoCapturePacket.Action[] ACTIONS = PlayerVideoCapturePacket.Action.values();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, PlayerVideoCapturePacket packet) {
        buffer.writeByte(packet.getAction().ordinal());
        if (packet.getAction().equals(PlayerVideoCapturePacket.Action.START_VIDEO_CAPTURE)) {
            buffer.writeIntLE(packet.getFrameRate());
            helper.writeString(buffer, packet.getFilePrefix());
        }
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, PlayerVideoCapturePacket packet) {
        int index = buffer.readByte();
        if (index >= ACTIONS.length) {
            packet.setAction(PlayerVideoCapturePacket.Action.UNKNOWN);
        } else {
            packet.setAction(ACTIONS[index]);
        }

        if (packet.getAction().equals(PlayerVideoCapturePacket.Action.START_VIDEO_CAPTURE)) {
            packet.setFrameRate(buffer.readIntLE());
            packet.setFilePrefix(helper.readString(buffer));
        }
    }
}
