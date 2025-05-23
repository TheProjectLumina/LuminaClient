package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class UpdateEquipPacket implements BedrockPacket {
    public short windowId;
    public short windowType;
    public int size; // Couldn't find anything on this one. Looks like it isn't used?
    public long uniqueEntityId;
    public NbtMap tag;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.UPDATE_EQUIP;
    }

    @Override
    public UpdateEquipPacket clone() {
        try {
            return (UpdateEquipPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

