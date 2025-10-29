package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.DebugShape;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.List;

/**
 * Sends debug geometry to the client. Meant for script debugging purposes.
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ServerScriptDebugDrawerPacket implements BedrockPacket {

    public final List<DebugShape> shapes = new ObjectArrayList<>();

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SERVER_SCRIPT_DEBUG_DRAWER;
    }

    @Override
    public BedrockPacket clone() {
        try {
            return (ServerScriptDebugDrawerPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
