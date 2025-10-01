package org.cloudburstmc.protocol.bedrock.data.camera;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;

import java.util.List;

@Data
public class CameraAimAssistCategories {
    private String identifier;
    private final List<CameraAimAssistCategory> categories = new ObjectArrayList<>();
}