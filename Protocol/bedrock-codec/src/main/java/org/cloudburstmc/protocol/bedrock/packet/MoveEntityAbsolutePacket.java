package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class MoveEntityAbsolutePacket implements BedrockPacket {
    public long runtimeEntityId;
    public Vector3f position;
    public Vector3f rotation;
    public boolean onGround;
    public boolean teleported;
    public boolean forceMove;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.MOVE_ENTITY_ABSOLUTE;
    }

    @Override
    public MoveEntityAbsolutePacket clone() {
        try {
            return (MoveEntityAbsolutePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

