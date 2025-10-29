package org.cloudburstmc.protocol.bedrock.data.camera;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CameraAimAssistPriority {
    private final String name;
    private final int priority;
}
