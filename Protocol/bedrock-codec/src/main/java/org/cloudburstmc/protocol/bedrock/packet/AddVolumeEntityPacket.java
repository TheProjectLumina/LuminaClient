package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class AddVolumeEntityPacket implements BedrockPacket {
    public int id;
    public NbtMap data;
    /**
     * @since v465
     */
    public String engineVersion;
    /**
     * @since v485
     */
    public String identifier;
    /**
     * @since v485
     */
    public String instanceName;

    /**
     * @since v503
     */
    public Vector3i minBounds;
    /**
     * @since v503
     */
    public Vector3i maxBounds;
    /**
     * @since v503
     */
    public int dimension;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.ADD_VOLUME_ENTITY;
    }

    @Override
    public AddVolumeEntityPacket clone() {
        try {
            return (AddVolumeEntityPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

