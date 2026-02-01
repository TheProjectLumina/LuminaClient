package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.*;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.cloudburstmc.protocol.common.util.OptionalBoolean;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true, exclude = {"itemDefinitions", "blockPalette"})
public class StartGamePacket implements BedrockPacket {
    public final List<GameRuleData<?>> gamerules = new ObjectArrayList<>();
    public long uniqueEntityId;
    public long runtimeEntityId;
    public GameType playerGameType;
    public Vector3f playerPosition;
    public Vector2f rotation;
    // Level settings start
    public long seed;
    public SpawnBiomeType spawnBiomeType;
    public String customBiomeName;
    public int dimensionId;
    public int generatorId;
    public GameType levelGameType;
    public int difficulty;
    public Vector3i defaultSpawn;
    public boolean achievementsDisabled;
    public int dayCycleStopTime;
    public int eduEditionOffers;
    public boolean eduFeaturesEnabled;
    public String educationProductionId;
    public float rainLevel;
    public float lightningLevel;
    public boolean platformLockedContentConfirmed;
    public boolean multiplayerGame;
    public boolean broadcastingToLan;
    public GamePublishSetting xblBroadcastMode;
    public GamePublishSetting platformBroadcastMode;
    public boolean commandsEnabled;
    public boolean texturePacksRequired;
    public final List<ExperimentData> experiments = new ObjectArrayList<>();
    public boolean experimentsPreviouslyToggled;
    public boolean bonusChestEnabled;
    public boolean startingWithMap;
    public boolean trustingPlayers;
    public PlayerPermission defaultPlayerPermission;
    public int serverChunkTickRange;
    public boolean behaviorPackLocked;
    public boolean resourcePackLocked;
    public boolean fromLockedWorldTemplate;
    public boolean usingMsaGamertagsOnly;
    public boolean fromWorldTemplate;
    public boolean worldTemplateOptionLocked;
    public boolean onlySpawningV1Villagers;
    public String vanillaVersion;
    public int limitedWorldWidth;
    public int limitedWorldHeight;
    public boolean netherType;
    /**
     * @since v465
     */
    public EduSharedUriResource eduSharedUriResource = EduSharedUriResource.EMPTY;
    public OptionalBoolean forceExperimentalGameplay;
    /**
     * @since 1.19.20
     */
    public ChatRestrictionLevel chatRestrictionLevel;
    /**
     * @since 1.19.20
     */
    public boolean disablingPlayerInteractions;
    /**
     * @since 1.19.20
     */
    public boolean disablingPersonas;
    /**
     * @since 1.19.20
     */
    public boolean disablingCustomSkins;
    // Level settings end
    public String levelId;
    public CharSequence levelName;
    public String premiumWorldTemplateId;
    public boolean trial;
    // SyncedPlayerMovementSettings start
    /**
     * @deprecated since v818. {@link AuthoritativeMovementMode#SERVER_WITH_REWIND} is now the default movement mode.
     */
    public AuthoritativeMovementMode authoritativeMovementMode;
    public int rewindHistorySize;
    boolean serverAuthoritativeBlockBreaking;
    // SyncedPlayerMovementSettings end
    public long currentTick;
    public int enchantmentSeed;
    public NbtList<NbtMap> blockPalette;
    public final List<BlockPropertyData> blockProperties = new ObjectArrayList<>();
    /**
     * @deprecated since v776. Use ItemComponentPacket instead.
     */
    public List<ItemDefinition> itemDefinitions = new ObjectArrayList<>();
    public String multiplayerCorrelationId;
    /**
     * @since v407
     */
    public boolean inventoriesServerAuthoritative;
    /**
     * The name of the server software.
     * Used for telemetry within the Bedrock client.
     *
     * @since v440
     */
    public String serverEngine;
    /**
     * @since v527
     */
    public NbtMap playerPropertyData;
    /**
     * A XXHash64 of all block states by their compound tag.
     * <b>The exact way this is calculated is not currently known.</b>
     * <p>
     * A value of 0 will not be validated by the client.
     *
     * @since v475
     */
    public long blockRegistryChecksum;
    /**
     * @since v527
     */
    public UUID worldTemplateId;
    /**
     * @since v534
     */
    public WorldType editorWorldType = WorldType.NON_EDITOR;
    /**
     * Enables client side chunk generation
     *
     * @since 1.19.20
     */
    public boolean clientSideGenerationEnabled;
    /**
     * @since v567
     */
    public boolean emoteChatMuted;
    /**
     * Whether block runtime IDs should be replaced by 32-bit integer hashes of NBT block state.
     * Unlike runtime IDs, this hashes should be persistent across versions and should make support for data-driven/custom blocks easier.
     *
     * @since v582
     */
    public boolean blockNetworkIdsHashed;
    /**
     * @since v582
     */
    public boolean createdInEditor;
    /**
     * @since v582
     */
    public boolean exportedFromEditor;
    /**
     * @since v589
     */
    public NetworkPermissions networkPermissions = NetworkPermissions.DEFAULT;
    /**
     * @since v671
     */
    public boolean hardcore;
    /**
     * @since v685
     */
    public String serverId;
    /**
     * @since v685
     */
    public String worldId;
    /**
     * @since v685
     */
    public String scenarioId;
    /**
     * @since v818
     */
    public String ownerId;
    /**
     * @since v827
     */
    public boolean tickDeathSystemsEnabled;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.START_GAME;
    }

    @Override
    public StartGamePacket clone() {
        try {
            return (StartGamePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public String getLevelName() {
        return getLevelName(String.class);
    }

    public <T extends CharSequence> T getLevelName(Class<T> type) {
        return type.cast(levelName);
    }
}

