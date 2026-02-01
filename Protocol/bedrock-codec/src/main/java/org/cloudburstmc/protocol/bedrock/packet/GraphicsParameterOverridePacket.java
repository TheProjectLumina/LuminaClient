package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.GraphicsOverrideParameterType;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.Map;

/**
 * Sent from the server to the client when a server script changes the rendering settings
 *
 * @since v859
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class GraphicsParameterOverridePacket implements BedrockPacket {

    public String biomeIdentifier;
    public GraphicsOverrideParameterType parameterType;
    public Map<Float, Vector3f> values;
    public boolean reset;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.GRAPHICS_PARAMETER_OVERRIDE_PACKET;
    }

    @Override
    public GraphicsParameterOverridePacket clone() {
        try {
            return (GraphicsParameterOverridePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
