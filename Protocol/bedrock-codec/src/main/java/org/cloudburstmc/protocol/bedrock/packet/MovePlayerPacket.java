package org.cloudburstmc.protocol.bedrock.packet;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class MovePlayerPacket implements BedrockPacket {
    public long runtimeEntityId;
    public Vector3f position;
    public Vector3f rotation;
    public Mode mode;
    public boolean onGround;
    public long ridingRuntimeEntityId;
    public TeleportationCause teleportationCause;
    public int entityType;
    public long tick;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.MOVE_PLAYER;
    }

    public enum Mode {
        NORMAL,
        RESPAWN,
        TELEPORT,
        HEAD_ROTATION
    }

    public enum TeleportationCause {
        UNKNOWN,
        PROJECTILE,
        CHORUS_FRUIT,
        COMMAND,
        BEHAVIOR;

        public static final InternalLogger log = InternalLoggerFactory.getInstance(TeleportationCause.class);

        public static final TeleportationCause[] VALUES = values();

        public static TeleportationCause byId(int id) {
            if (id >= 0 && id < VALUES.length) {
                return VALUES[id];
            }
            log.debug("Unknown teleportation cause ID: {}", id);
            return UNKNOWN;
        }
    }

    @Override
    public MovePlayerPacket clone() {
        try {
            return (MovePlayerPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

