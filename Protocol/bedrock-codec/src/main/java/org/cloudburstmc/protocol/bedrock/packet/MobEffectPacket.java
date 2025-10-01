package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class MobEffectPacket implements BedrockPacket {
    public long runtimeEntityId;
    public Event event;
    public int effectId;
    public int amplifier;
    public boolean particles;
    public int duration;
    /**
     * @since v662
     */
    public long tick;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.MOB_EFFECT;
    }

    public enum Event {
        NONE,
        ADD,
        MODIFY,
        REMOVE,
    }

    @Override
    public MobEffectPacket clone() {
        try {
            return (MobEffectPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

