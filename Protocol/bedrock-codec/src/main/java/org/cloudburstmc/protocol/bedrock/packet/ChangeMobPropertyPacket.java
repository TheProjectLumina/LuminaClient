package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Server-bound packet to change the properties of a mob.
 *
 * @since v503
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ChangeMobPropertyPacket implements BedrockPacket {
    public long uniqueEntityId;
    public String property;
    public boolean boolValue;
    public String stringValue;
    public int intValue;
    public float floatValue;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.CHANGE_MOB_PROPERTY;
    }

    @Override
    public ChangeMobPropertyPacket clone() {
        try {
            return (ChangeMobPropertyPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

