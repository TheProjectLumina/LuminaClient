package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.cloudburstmc.protocol.bedrock.data.ExperimentData;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.List;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ResourcePackStackPacket implements BedrockPacket {
    public boolean forcedToAccept;
    public final List<Entry> behaviorPacks = new ObjectArrayList<>();
    public final List<Entry> resourcePacks = new ObjectArrayList<>();
    public String gameVersion;
    public final List<ExperimentData> experiments = new ObjectArrayList<>();
    public boolean experimentsPreviouslyToggled;
    /**
     * @since v671
     */
    public boolean hasEditorPacks;


    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.RESOURCE_PACK_STACK;
    }

    @Value
    public static class Entry {
        public final String packId;
        public final String packVersion;
        public final String subPackName;
    }

    @Override
    public ResourcePackStackPacket clone() {
        try {
            return (ResourcePackStackPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

