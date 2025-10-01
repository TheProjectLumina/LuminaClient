package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.awt.*;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class PlayerListPacket implements BedrockPacket {
    public final List<Entry> entries = new ObjectArrayList<>();
    public Action action;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.PLAYER_LIST;
    }

    public enum Action {
        ADD,
        REMOVE
    }


    @Data
    @ToString(doNotUseGetters = true)
    @EqualsAndHashCode(doNotUseGetters = true)
    public final static class Entry {
        public final UUID uuid;
        public long entityId;
        public CharSequence name;
        public String xuid;
        public String platformChatId;
        public int buildPlatform;
        public SerializedSkin skin;
        public boolean teacher;
        public boolean host;
        public boolean trustedSkin;
        public boolean subClient;
        public int color;

        public String getName() {
            return getName(String.class);
        }

        public <T extends CharSequence> T getName(Class<T> type) {
            return type.cast(name);
        }
    }

    @Override
    public PlayerListPacket clone() {
        try {
            return (PlayerListPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

