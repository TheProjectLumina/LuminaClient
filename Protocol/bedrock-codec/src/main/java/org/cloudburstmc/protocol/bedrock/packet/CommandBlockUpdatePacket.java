package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.CommandBlockMode;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class CommandBlockUpdatePacket implements BedrockPacket {
    public boolean block;
    public Vector3i blockPosition;
    public CommandBlockMode mode;
    public boolean redstoneMode;
    public boolean conditional;
    public long minecartRuntimeEntityId;
    public String command;
    public String lastOutput;
    public String name;
    public String filteredName;
    public boolean outputTracked;
    public long tickDelay;
    public boolean executingOnFirstTick;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.COMMAND_BLOCK_UPDATE;
    }

    @Override
    public CommandBlockUpdatePacket clone() {
        try {
            return (CommandBlockUpdatePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

