package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.EmoteFlag;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.EnumSet;
import java.util.Set;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class EmotePacket implements BedrockPacket {
    public long runtimeEntityId;
    /**
     * @since v589
     */
    public String xuid;
    /**
     * @since 589
     */
    public String platformId;
    public String emoteId;
    public final Set<EmoteFlag> flags = EnumSet.noneOf(EmoteFlag.class);
    /**
     * @since v729
     */
    public int emoteDuration;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.EMOTE;
    }

    @Override
    public EmotePacket clone() {
        try {
            return (EmotePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

