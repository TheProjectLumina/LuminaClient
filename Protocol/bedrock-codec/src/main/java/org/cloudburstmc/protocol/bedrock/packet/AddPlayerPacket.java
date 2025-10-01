package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.data.PlayerAbilityHolder;
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission;
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityLinkData;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityProperties;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class AddPlayerPacket implements BedrockPacket, PlayerAbilityHolder {
    public EntityDataMap metadata = new EntityDataMap();
    public List<EntityLinkData> entityLinks = new ObjectArrayList<>();
    public UUID uuid;
    public String username;
    public long uniqueEntityId;
    public long runtimeEntityId;
    public String platformChatId;
    public Vector3f position;
    public Vector3f motion;
    public Vector3f rotation;
    public ItemData hand;
    public AdventureSettingsPacket adventureSettings = new AdventureSettingsPacket();
    public String deviceId;
    public int buildPlatform;
    public GameType gameType;

    /**
     * @since v534
     */
    public List<AbilityLayer> abilityLayers = new ObjectArrayList<>();
    /**
     * @since v557
     */
    public final EntityProperties properties = new EntityProperties();

    public void setUniqueEntityId(long uniqueEntityId) {
        this.uniqueEntityId = uniqueEntityId;
        this.adventureSettings.setUniqueEntityId(uniqueEntityId);
    }

    @Override
    public PlayerPermission getPlayerPermission() {
        return this.adventureSettings.getPlayerPermission();
    }

    @Override
    public void setPlayerPermission(PlayerPermission playerPermission) {
        this.adventureSettings.setPlayerPermission(playerPermission);
    }

    @Override
    public CommandPermission getCommandPermission() {
        return this.adventureSettings.getCommandPermission();
    }

    @Override
    public void setCommandPermission(CommandPermission commandPermission) {
        this.adventureSettings.setCommandPermission(commandPermission);
    }

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.ADD_PLAYER;
    }

    @Override
    public AddPlayerPacket clone() {
        try {
            return (AddPlayerPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

