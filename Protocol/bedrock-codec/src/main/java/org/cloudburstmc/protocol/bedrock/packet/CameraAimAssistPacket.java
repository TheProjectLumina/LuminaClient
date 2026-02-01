package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.protocol.bedrock.data.camera.AimAssistAction;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class CameraAimAssistPacket implements BedrockPacket {
    public Vector2f viewAngle;
    public float distance;
    public TargetMode targetMode;
    public AimAssistAction action;
    /**
     * @since v766
     */
    public String presetId;
    /**
     * @since v827
     */
    public boolean showDebugRender;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.CAMERA_AIM_ASSIST;
    }

    @Override
    public CameraAimAssistPacket clone() {
        try {
            return (CameraAimAssistPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public enum TargetMode {
        ANGLE,
        DISTANCE
    }
}
