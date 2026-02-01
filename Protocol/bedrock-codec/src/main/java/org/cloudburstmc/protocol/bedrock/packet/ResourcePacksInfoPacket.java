package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.*;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ResourcePacksInfoPacket implements BedrockPacket {
    /**
     * @deprecated since v729
     */
    public final List<Entry> behaviorPackInfos = new ObjectArrayList<>();
    public final List<Entry> resourcePackInfos = new ObjectArrayList<>();
    public boolean forcedToAccept;
    /**
     * @since v662
     */
    public boolean hasAddonPacks;
    public boolean scriptingEnabled;
    /**
     * @since v448
     * @deprecated since v729
     */
    public boolean forcingServerPacksEnabled;
    /**
     * @since v766
     */
    public UUID worldTemplateId;
    /**
     * @since v766
     */
    public String worldTemplateVersion;
    /**
     * Force the client to disable vibrant visuals, even if the client supports it.
     *
     * @since v818
     */
    public boolean vibrantVisualsForceDisabled;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.RESOURCE_PACKS_INFO;
    }

    @Data
    @AllArgsConstructor
    public static class Entry {
        public UUID packId;
        public String packVersion;
        public long packSize;
        public String contentKey;
        public String subPackName;
        public String contentId;
        public boolean scripting;
        public boolean raytracingCapable;
        /**
         * @since v712
         */
        public boolean addonPack;
        /**
         * @since v748
         */
        public String cdnUrl;
    }

    @Override
    public ResourcePacksInfoPacket clone() {
        try {
            return (ResourcePacksInfoPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

