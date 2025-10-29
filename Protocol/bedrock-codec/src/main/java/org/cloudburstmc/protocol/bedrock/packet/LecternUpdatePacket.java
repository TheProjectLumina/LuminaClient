package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class LecternUpdatePacket implements BedrockPacket {
    public int page;
    public int totalPages;
    public Vector3i blockPosition;
    /**
     * @deprecated since v662
     */
    @Deprecated
    public boolean droppingBook;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.LECTERN_UPDATE;
    }

    @Override
    public LecternUpdatePacket clone() {
        try {
            return (LecternUpdatePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

