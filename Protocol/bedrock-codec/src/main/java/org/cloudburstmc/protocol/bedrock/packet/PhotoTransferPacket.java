package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.PhotoType;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class PhotoTransferPacket implements BedrockPacket {
    public String name;
    public byte[] data;
    public String bookId;
    /**
     * @since v465
     */
    public PhotoType photoType;
    /**
     * @since v465
     */
    public PhotoType sourceType;
    /**
     * @since v465
     */
    public long ownerId;
    /**
     * @since v465
     */
    public String newPhotoName;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.PHOTO_TRANSFER;
    }

    @Override
    public PhotoTransferPacket clone() {
        try {
            return (PhotoTransferPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

