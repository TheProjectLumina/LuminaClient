package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.Optional;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SpawnParticleEffectPacket implements BedrockPacket {
    public int dimensionId;
    public long uniqueEntityId = -1;
    public Vector3f position;
    public String identifier;
    public Optional<String> molangVariablesJson;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SPAWN_PARTICLE_EFFECT;
    }

    @Override
    public SpawnParticleEffectPacket clone() {
        try {
            return (SpawnParticleEffectPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

