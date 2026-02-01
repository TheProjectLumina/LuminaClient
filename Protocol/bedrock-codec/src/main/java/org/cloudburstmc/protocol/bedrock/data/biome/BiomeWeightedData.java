package org.cloudburstmc.protocol.bedrock.data.biome;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.cloudburstmc.protocol.common.util.index.Indexable;
import org.cloudburstmc.protocol.common.util.index.Unindexed;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeWeightedData {
    @Getter(AccessLevel.NONE)
    transient Indexable<String> biome;
    int weight;

    @JsonCreator
    public BiomeWeightedData(String biome, int weight) {
        this.biome = new Unindexed<>(biome);
        this.weight = weight;
    }

    public String getBiome() {
        return biome.get();
    }
}
