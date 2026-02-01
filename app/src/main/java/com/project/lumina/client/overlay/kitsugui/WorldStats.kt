package com.project.lumina.client.overlay.kitsugui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.ui.theme.KitsuPrimary
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import com.project.lumina.client.R
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.nbt.NbtList
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.protocol.bedrock.data.*
import org.cloudburstmc.protocol.common.util.OptionalBoolean
import java.util.UUID

@Composable
fun WorldStats() {
    var worldName by remember { mutableStateOf<String?>(null) }
    var levelName by remember { mutableStateOf<String?>(null) }
    var levelId by remember { mutableStateOf<String?>(null) }
    var gameMode by remember { mutableStateOf<String?>(null) }
    var vanillaVersion by remember { mutableStateOf<String?>(null) }
    var uniqueEntityId by remember { mutableStateOf<Long?>(null) }
    var runtimeEntityId by remember { mutableStateOf<Long?>(null) }
    var playerGameType by remember { mutableStateOf<String?>(null) }
    var playerPosition by remember { mutableStateOf<String?>(null) }
    var rotation by remember { mutableStateOf<Vector2f?>(null) }
    var seed by remember { mutableStateOf<Long?>(null) }
    var spawnBiomeType by remember { mutableStateOf<SpawnBiomeType?>(null) }
    var customBiomeName by remember { mutableStateOf<String?>(null) }
    var dimensionId by remember { mutableStateOf<Int?>(null) }
    var generatorId by remember { mutableStateOf<Int?>(null) }
    var levelGameType by remember { mutableStateOf<GameType?>(null) }
    var difficulty by remember { mutableStateOf<Int?>(null) }
    var defaultSpawn by remember { mutableStateOf<String?>(null) }
    var achievementsDisabled by remember { mutableStateOf<Boolean?>(null) }
    var dayCycleStopTime by remember { mutableStateOf<Int?>(null) }
    var eduEditionOffers by remember { mutableStateOf<Int?>(null) }
    var eduFeaturesEnabled by remember { mutableStateOf<Boolean?>(null) }
    var educationProductionId by remember { mutableStateOf<String?>(null) }
    var rainLevel by remember { mutableStateOf<Float?>(null) }
    var lightningLevel by remember { mutableStateOf<Float?>(null) }
    var platformLockedContentConfirmed by remember { mutableStateOf<Boolean?>(null) }
    var multiplayerGame by remember { mutableStateOf<Boolean?>(null) }
    var broadcastingToLan by remember { mutableStateOf<Boolean?>(null) }
    var xblBroadcastMode by remember { mutableStateOf<GamePublishSetting?>(null) }
    var platformBroadcastMode by remember { mutableStateOf<GamePublishSetting?>(null) }
    var commandsEnabled by remember { mutableStateOf<Boolean?>(null) }
    var texturePacksRequired by remember { mutableStateOf<Boolean?>(null) }
    var experiments by remember { mutableStateOf<List<*>?>(null) }
    var experimentsPreviouslyToggled by remember { mutableStateOf<Boolean?>(null) }
    var bonusChestEnabled by remember { mutableStateOf<Boolean?>(null) }
    var startingWithMap by remember { mutableStateOf<Boolean?>(null) }
    var trustingPlayers by remember { mutableStateOf<Boolean?>(null) }
    var defaultPlayerPermission by remember { mutableStateOf<PlayerPermission?>(null) }
    var serverChunkTickRange by remember { mutableStateOf<Int?>(null) }
    var behaviorPackLocked by remember { mutableStateOf<Boolean?>(null) }
    var resourcePackLocked by remember { mutableStateOf<Boolean?>(null) }
    var fromLockedWorldTemplate by remember { mutableStateOf<Boolean?>(null) }
    var usingMsaGamertagsOnly by remember { mutableStateOf<Boolean?>(null) }
    var fromWorldTemplate by remember { mutableStateOf<Boolean?>(null) }
    var worldTemplateOptionLocked by remember { mutableStateOf<Boolean?>(null) }
    var onlySpawningV1Villagers by remember { mutableStateOf<Boolean?>(null) }
    var limitedWorldWidth by remember { mutableStateOf<Int?>(null) }
    var limitedWorldHeight by remember { mutableStateOf<Int?>(null) }
    var netherType by remember { mutableStateOf<Boolean?>(null) }
    var eduSharedUriResource by remember { mutableStateOf<EduSharedUriResource?>(null) }
    var forceExperimentalGameplay by remember { mutableStateOf<OptionalBoolean?>(null) }
    var chatRestrictionLevel by remember { mutableStateOf<ChatRestrictionLevel?>(null) }
    var disablingPlayerInteractions by remember { mutableStateOf<Boolean?>(null) }
    var disablingPersonas by remember { mutableStateOf<Boolean?>(null) }
    var disablingCustomSkins by remember { mutableStateOf<Boolean?>(null) }
    var premiumWorldTemplateId by remember { mutableStateOf<String?>(null) }
    var trial by remember { mutableStateOf<Boolean?>(null) }
    var authoritativeMovementMode by remember { mutableStateOf<AuthoritativeMovementMode?>(null) }
    var rewindHistorySize by remember { mutableStateOf<Int?>(null) }
    var serverAuthoritativeBlockBreaking by remember { mutableStateOf<Boolean?>(null) }
    var currentTick by remember { mutableStateOf<Long?>(null) }
    var enchantmentSeed by remember { mutableStateOf<Int?>(null) }
    var blockPalette by remember { mutableStateOf<NbtList<*>?>(null) }
    var blockProperties by remember { mutableStateOf<List<*>?>(null) }
    var itemDefinitions by remember { mutableStateOf<List<*>?>(null) }
    var multiplayerCorrelationId by remember { mutableStateOf<String?>(null) }
    var inventoriesServerAuthoritative by remember { mutableStateOf<Boolean?>(null) }
    var playerPropertyData by remember { mutableStateOf<NbtMap?>(null) }
    var blockRegistryChecksum by remember { mutableStateOf<Long?>(null) }
    var worldTemplateId by remember { mutableStateOf<UUID?>(null) }
    var worldEditor by remember { mutableStateOf<Boolean?>(null) }
    var clientSideGenerationEnabled by remember { mutableStateOf<Boolean?>(null) }
    var emoteChatMuted by remember { mutableStateOf<Boolean?>(null) }
    var blockNetworkIdsHashed by remember { mutableStateOf<Boolean?>(null) }
    var createdInEditor by remember { mutableStateOf<Boolean?>(null) }
    var exportedFromEditor by remember { mutableStateOf<Boolean?>(null) }
    var networkPermissions by remember { mutableStateOf<NetworkPermissions?>(null) }
    var hardcore by remember { mutableStateOf<Boolean?>(null) }
    var serverId by remember { mutableStateOf<String?>(null) }
    var worldId by remember { mutableStateOf<String?>(null) }
    var scenarioId by remember { mutableStateOf<String?>(null) }
    var gamerules by remember { mutableStateOf<List<*>?>(null) }
    var serverEngine by remember { mutableStateOf<String?>(null) }
    var worldSpawn by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasData by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val netBound = GameManager.netBound
                if (netBound != null) {
                    val gameDataManager = netBound.gameDataManager
                    if (gameDataManager.hasStartGameData()) {
                        worldName = gameDataManager.getWorldName()
                        levelName = gameDataManager.getLevelName()
                        levelId = gameDataManager.getLevelId()
                        gameMode = gameDataManager.getGameMode()?.toString()
                        vanillaVersion = gameDataManager.getVanillaVersion()
                        uniqueEntityId = gameDataManager.getUniqueEntityId()
                        runtimeEntityId = gameDataManager.getRuntimeEntityId()
                        playerGameType = gameDataManager.getPlayerGameType()?.toString()
                        playerPosition = gameDataManager.getPlayerPosition()?.let {
                            "X: ${it.x.toInt()}, Y: ${it.y.toInt()}, Z: ${it.z.toInt()}"
                        }
                        rotation = gameDataManager.getRotation()
                        seed = gameDataManager.getSeed()
                        spawnBiomeType = gameDataManager.getSpawnBiomeType()
                        customBiomeName = gameDataManager.getCustomBiomeName()
                        dimensionId = gameDataManager.getDimensionId()
                        generatorId = gameDataManager.getGeneratorId()
                        levelGameType = gameDataManager.getLevelGameType()
                        difficulty = gameDataManager.getDifficulty()
                        defaultSpawn = gameDataManager.getDefaultSpawn()?.let {
                            "X: ${it.x}, Y: ${it.y}, Z: ${it.z}"
                        }
                        achievementsDisabled = gameDataManager.getAchievementsDisabled()
                        dayCycleStopTime = gameDataManager.getDayCycleStopTime()
                        eduEditionOffers = gameDataManager.getEduEditionOffers()
                        eduFeaturesEnabled = gameDataManager.getEduFeaturesEnabled()
                        educationProductionId = gameDataManager.getEducationProductionId()
                        rainLevel = gameDataManager.getRainLevel()
                        lightningLevel = gameDataManager.getLightningLevel()
                        platformLockedContentConfirmed = gameDataManager.getPlatformLockedContentConfirmed()
                        multiplayerGame = gameDataManager.getMultiplayerGame()
                        broadcastingToLan = gameDataManager.getBroadcastingToLan()
                        xblBroadcastMode = gameDataManager.getXblBroadcastMode()
                        platformBroadcastMode = gameDataManager.getPlatformBroadcastMode()
                        commandsEnabled = gameDataManager.getCommandsEnabled()
                        texturePacksRequired = gameDataManager.getTexturePacksRequired()
                        experiments = gameDataManager.getExperiments()
                        experimentsPreviouslyToggled = gameDataManager.getExperimentsPreviouslyToggled()
                        bonusChestEnabled = gameDataManager.getBonusChestEnabled()
                        startingWithMap = gameDataManager.getStartingWithMap()
                        trustingPlayers = gameDataManager.getTrustingPlayers()
                        defaultPlayerPermission = gameDataManager.getDefaultPlayerPermission()
                        serverChunkTickRange = gameDataManager.getServerChunkTickRange()
                        behaviorPackLocked = gameDataManager.getBehaviorPackLocked()
                        resourcePackLocked = gameDataManager.getResourcePackLocked()
                        fromLockedWorldTemplate = gameDataManager.getFromLockedWorldTemplate()
                        usingMsaGamertagsOnly = gameDataManager.getUsingMsaGamertagsOnly()
                        fromWorldTemplate = gameDataManager.getFromWorldTemplate()
                        worldTemplateOptionLocked = gameDataManager.getWorldTemplateOptionLocked()
                        onlySpawningV1Villagers = gameDataManager.getOnlySpawningV1Villagers()
                        limitedWorldWidth = gameDataManager.getLimitedWorldWidth()
                        limitedWorldHeight = gameDataManager.getLimitedWorldHeight()
                        netherType = gameDataManager.getNetherType()
                        eduSharedUriResource = gameDataManager.getEduSharedUriResource()
                        forceExperimentalGameplay = gameDataManager.getForceExperimentalGameplay()
                        chatRestrictionLevel = gameDataManager.getChatRestrictionLevel()
                        disablingPlayerInteractions = gameDataManager.getDisablingPlayerInteractions()
                        disablingPersonas = gameDataManager.getDisablingPersonas()
                        disablingCustomSkins = gameDataManager.getDisablingCustomSkins()
                        premiumWorldTemplateId = gameDataManager.getPremiumWorldTemplateId()
                        trial = gameDataManager.getTrial()
                        authoritativeMovementMode = gameDataManager.getAuthoritativeMovementMode()
                        rewindHistorySize = gameDataManager.getRewindHistorySize()
                        serverAuthoritativeBlockBreaking = gameDataManager.getServerAuthoritativeBlockBreaking()
                        currentTick = gameDataManager.getCurrentTick()
                        enchantmentSeed = gameDataManager.getEnchantmentSeed()
                        blockPalette = gameDataManager.getBlockPalette()
                        blockProperties = gameDataManager.getBlockProperties()
                        itemDefinitions = gameDataManager.getItemDefinitions()
                        multiplayerCorrelationId = gameDataManager.getMultiplayerCorrelationId()
                        inventoriesServerAuthoritative = gameDataManager.getInventoriesServerAuthoritative()
                        playerPropertyData = gameDataManager.getPlayerPropertyData()
                        blockRegistryChecksum = gameDataManager.getBlockRegistryChecksum()
                        worldTemplateId = gameDataManager.getWorldTemplateId()
                        worldEditor = gameDataManager.getWorldEditor()
                        clientSideGenerationEnabled = gameDataManager.getClientSideGenerationEnabled()
                        emoteChatMuted = gameDataManager.getEmoteChatMuted()
                        blockNetworkIdsHashed = gameDataManager.getBlockNetworkIdsHashed()
                        createdInEditor = gameDataManager.getCreatedInEditor()
                        exportedFromEditor = gameDataManager.getExportedFromEditor()
                        networkPermissions = gameDataManager.getNetworkPermissions()
                        hardcore = gameDataManager.getHardcore()
                        serverId = gameDataManager.getServerId()
                        worldId = gameDataManager.getWorldId()
                        scenarioId = gameDataManager.getScenarioId()
                        gamerules = gameDataManager.getGamerules()
                        serverEngine = gameDataManager.getServerEngine()
                        worldSpawn = gameDataManager.getWorldSpawn()?.let {
                            "X: ${it.x}, Y: ${it.y}, Z: ${it.z}"
                        }
                        hasData = true
                        isLoading = false
                    } else {
                        hasData = false
                        isLoading = false
                    }
                } else {
                    hasData = false
                    isLoading = false
                }
            } catch (e: Exception) {
                hasData = false
                isLoading = false
            }
            delay(2.seconds)
        }
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            KitsuPrimary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_world_code),
                        contentDescription = null,
                        tint = KitsuPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Server Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_server_2),
                        contentDescription = null,
                        tint = KitsuPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = vanillaVersion?.let { "v$it" } ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally),
                    color = KitsuPrimary
                )
            } else if (!hasData) {
                Text(
                    "No world data available",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                val fields = listOf(
                    Triple("World Name", worldName ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_world),
                    Triple("Level Name", levelName ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_book),
                    Triple("Level ID", levelId ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_id),
                    Triple("Game Mode", gameMode?.let { formatGameMode(it) } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_device_gamepad),
                    Triple("Vanilla Version", vanillaVersion ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_code),
                    Triple("Unique Entity ID", uniqueEntityId?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_fingerprint),
                    Triple("Runtime Entity ID", runtimeEntityId?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_tag),
                    Triple("Player Game Type", playerGameType?.let { formatGameMode(it) } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_device_gamepad_3),
                    Triple("Player Position", playerPosition ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_location),
                    Triple("Rotation", rotation?.let { "Pitch: ${it.x}, Yaw: ${it.y}" } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_compass),
                    Triple("Seed", seed?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_seeding),
                    Triple("Spawn Biome Type", spawnBiomeType?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_tree),
                    Triple("Custom Biome Name", customBiomeName ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_leaf),
                    Triple("Dimension ID", dimensionId?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_world_cog),
                    Triple("Generator ID", generatorId?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_settings),
                    Triple("Level Game Type", levelGameType?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_device_gamepad_2),
                    Triple("Difficulty", difficulty?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_sword),
                    Triple("Default Spawn", defaultSpawn ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_home),
                    Triple("Achievements Disabled", achievementsDisabled?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_trophy_off),
                    Triple("Day Cycle Stop Time", dayCycleStopTime?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_clock),
                    Triple("Edu Edition Offers", eduEditionOffers?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_book),
                    Triple("Edu Features Enabled", eduFeaturesEnabled?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_school),
                    Triple("Education Production ID", educationProductionId ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_id),
                    Triple("Rain Level", rainLevel?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_droplets),
                    Triple("Lightning Level", lightningLevel?.toString() ?: "Unknown", R.drawable.flashthundericon_r0),
                    Triple("Platform Locked Content", platformLockedContentConfirmed?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_lock),
                    Triple("Multiplayer Game", multiplayerGame?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_users),
                    Triple("Broadcasting to LAN", broadcastingToLan?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_wifi),
                    Triple("XBL Broadcast Mode", xblBroadcastMode?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_broadcast),
                    Triple("Platform Broadcast Mode", platformBroadcastMode?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_broadcast),
                    Triple("Commands Enabled", commandsEnabled?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_terminal),
                    Triple("Texture Packs Required", texturePacksRequired?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_photo),
                    Triple("Experiments", experiments?.let { "${it.size} experiments" } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_flask),
                    Triple("Experiments Toggled", experimentsPreviouslyToggled?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_switch_vertical),
                    Triple("Bonus Chest Enabled", bonusChestEnabled?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_box),
                    Triple("Starting With Map", startingWithMap?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_map),
                    Triple("Trusting Players", trustingPlayers?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_shield),
                    Triple("Default Player Permission", defaultPlayerPermission?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_key),
                    Triple("Server Chunk Tick Range", serverChunkTickRange?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_server),
                    Triple("Behavior Pack Locked", behaviorPackLocked?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_lock),
                    Triple("Resource Pack Locked", resourcePackLocked?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_lock),
                    Triple("From Locked World Template", fromLockedWorldTemplate?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_lock),
                    Triple("Using MSA Gamertags Only", usingMsaGamertagsOnly?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_user_check),
                    Triple("From World Template", fromWorldTemplate?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_template),
                    Triple("World Template Option Locked", worldTemplateOptionLocked?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_lock),
                    Triple("Only Spawning V1 Villagers", onlySpawningV1Villagers?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_user),
                    Triple("Limited World Width", limitedWorldWidth?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_ruler),
                    Triple("Limited World Height", limitedWorldHeight?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_ruler),
                    Triple("Nether Type", netherType?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_flame),
                    Triple("Edu Shared URI Resource", eduSharedUriResource?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_link),
                    Triple("Force Experimental Gameplay", forceExperimentalGameplay?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_flask),
                    Triple("Chat Restriction Level", chatRestrictionLevel?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_message),
                    Triple("Disabling Player Interactions", disablingPlayerInteractions?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_user_off),
                    Triple("Disabling Personas", disablingPersonas?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_mask_off),
                    Triple("Disabling Custom Skins", disablingCustomSkins?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_photo_x),
                    Triple("Premium World Template ID", premiumWorldTemplateId ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_star),
                    Triple("Trial", trial?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_clock),
                    Triple("Authoritative Movement Mode", authoritativeMovementMode?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_run),
                    Triple("Rewind History Size", rewindHistorySize?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_rewind_forward_5),
                    Triple("Server Auth Block Breaking", serverAuthoritativeBlockBreaking?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_pick),
                    Triple("Current Tick", currentTick?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_clock),
                    Triple("Enchantment Seed", enchantmentSeed?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_sparkles),
                    Triple("Block Palette", blockPalette?.let { "${it.size} blocks" } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_cube),
                    Triple("Block Properties", blockProperties?.let { "${it.size} properties" } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_settings),
                    Triple("Item Definitions", itemDefinitions?.let { "${it.size} items" } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_box),
                    Triple("Multiplayer Correlation ID", multiplayerCorrelationId ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_link),
                    Triple("Inventories Server Auth", inventoriesServerAuthoritative?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_diamond),
                    Triple("Player Property Data", playerPropertyData?.let { "${it.size} properties" } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_user),
                    Triple("Block Registry Checksum", blockRegistryChecksum?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_hash),
                    Triple("World Template ID", worldTemplateId?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_template),
                    Triple("World Editor", worldEditor?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_edit),
                    Triple("Client Side Generation", clientSideGenerationEnabled?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_cpu),
                    Triple("Emote Chat Muted", emoteChatMuted?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_message_off),
                    Triple("Block Network IDs Hashed", blockNetworkIdsHashed?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_hash),
                    Triple("Created In Editor", createdInEditor?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_edit),
                    Triple("Exported From Editor", exportedFromEditor?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_package_export),
                    Triple("Network Permissions", networkPermissions?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_network),
                    Triple("Hardcore", hardcore?.toString() ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_skull),
                    Triple("Server ID", serverId ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_server),
                    Triple("World ID", worldId ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_world),
                    Triple("Scenario ID", scenarioId ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_id),
                    Triple("Game Rules", gamerules?.let { "${it.size} rules" } ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_book_2),
                    Triple("Server Engine", serverEngine ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_engine),
                    Triple("World Spawn", worldSpawn ?: "Unknown", ir.alirezaivaz.tablericons.R.drawable.ic_home)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fields.filter {
                        searchQuery.isEmpty() || it.first.contains(searchQuery, ignoreCase = true)
                    }.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { (name, value, iconRes) ->
                                var isHovered by remember { mutableStateOf(false) }
                                val backgroundColor by animateColorAsState(
                                    if (isHovered) KitsuPrimary.copy(alpha = 0.2f) else Color(0xFF1A1A1A)
                                )

                                OutlinedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                        .shadow(4.dp, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { /* Add click action if needed */ }
                                        .background(backgroundColor)
                                        .border(1.dp, KitsuPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Icon(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = null,
                                            tint = KitsuPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            Text(
                                                text = name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = value,
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.85f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatGameMode(gameMode: String): String {
    return when (gameMode.uppercase()) {
        "SURVIVAL" -> "Survival"
        "CREATIVE" -> "Creative"
        "ADVENTURE" -> "Adventure"
        "SPECTATOR" -> "Spectator"
        else -> gameMode.replaceFirstChar { it.uppercase() }
    }
}