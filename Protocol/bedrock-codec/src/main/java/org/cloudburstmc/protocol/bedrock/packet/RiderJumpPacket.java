package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * @deprecated Removed as of v800 (1.21.80). Server authoritative jump is handled by {@link PlayerAuthInputPacket}
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class RiderJumpPacket implements BedrockPacket {
    public int jumpStrength;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.RIDER_JUMP;
    }

    @Override
    public RiderJumpPacket clone() {
        try {
            return (RiderJumpPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

