package org.cloudburstmc.protocol.bedrock.data.definitions;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemVersion;

@Value
@NonFinal
@AllArgsConstructor
public class SimpleItemDefinition implements ItemDefinition {
    String identifier;
    int runtimeId;
    ItemVersion version;
    boolean componentBased;
    NbtMap componentData;

    // Backwards compatibility constructor
    public SimpleItemDefinition(String identifier, int runtimeId, boolean componentBased) {
        this.identifier = identifier;
        this.runtimeId = runtimeId;
        this.componentBased = componentBased;
        this.version = ItemVersion.LEGACY;
        this.componentData = null;
    }
}
