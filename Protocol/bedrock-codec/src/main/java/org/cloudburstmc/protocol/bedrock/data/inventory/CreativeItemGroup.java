package org.cloudburstmc.protocol.bedrock.data.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CreativeItemGroup {
    private final CreativeItemCategory category;
    private final String name;
    private final ItemData icon;
}
