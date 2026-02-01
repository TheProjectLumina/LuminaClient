package org.cloudburstmc.protocol.bedrock.data.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.cloudburstmc.math.vector.Vector2f;

@Data
@Builder
@AllArgsConstructor
public class CameraAimAssistPreset {
    private String identifier;
    private Integer targetMode;
    private Vector2f angle;
    private Float distance;
}
