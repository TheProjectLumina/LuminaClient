package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class NpcDialoguePacket implements BedrockPacket {

    public long uniqueEntityId;
    public Action action;
    public String dialogue;
    public String sceneName;
    public String npcName;
    public String actionJson;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.NPC_DIALOGUE;
    }

    public enum Action {
        OPEN,
        CLOSE
    }

    @Override
    public NpcDialoguePacket clone() {
        try {
            return (NpcDialoguePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

