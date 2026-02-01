package org.cloudburstmc.protocol.bedrock.data.definitions;

import lombok.Value;
import org.cloudburstmc.protocol.common.NamedDefinition;

@Value
public class SimpleNamedDefinition implements NamedDefinition {
    String identifier;
    int runtimeId;
}
