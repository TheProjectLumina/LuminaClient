package org.cloudburstmc.protocol.bedrock.data.camera;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CameraAimAssistCategory {
    private String name;
    private List<CameraAimAssistPriority> entityPriorities = new ObjectArrayList<>();
    private List<CameraAimAssistPriority> blockPriorities = new ArrayList<>();
    private Integer entityDefaultPriorities;
    private Integer blockDefaultPriorities;
}