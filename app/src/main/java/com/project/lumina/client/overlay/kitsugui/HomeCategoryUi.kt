package com.project.lumina.client.overlay.kitsugui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.overlay.mods.WorldStatsOverlay
import com.project.lumina.client.ui.theme.KitsuPrimary
import kotlinx.coroutines.delay
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.nbt.NbtList
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode
import org.cloudburstmc.protocol.bedrock.data.ChatRestrictionLevel
import org.cloudburstmc.protocol.bedrock.data.EduSharedUriResource
import org.cloudburstmc.protocol.bedrock.data.GamePublishSetting
import org.cloudburstmc.protocol.bedrock.data.GameType
import org.cloudburstmc.protocol.bedrock.data.NetworkPermissions
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.SpawnBiomeType
import org.cloudburstmc.protocol.common.util.OptionalBoolean
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeCategoryUi() {
    var levelName by remember { mutableStateOf<String?>(null) }
    var levelId by remember { mutableStateOf<String?>(null) }
    var gameMode by remember { mutableStateOf<String?>(null) }
    var vanillaVersion by remember { mutableStateOf<String?>(null) }
    var uniqueEntityId by remember { mutableStateOf<Long?>(null) }
    var runtimeEntityId by remember { mutableStateOf<Long?>(null) }
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

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val netBound = GameManager.netBound
                if (netBound != null) {
                    val gameDataManager = netBound.gameDataManager
                    if (gameDataManager.hasStartGameData()) {
                        levelName = gameDataManager.getLevelName()
                        levelId = gameDataManager.getLevelId()
                        gameMode = gameDataManager.getGameMode()?.toString()
                        vanillaVersion = gameDataManager.getVanillaVersion()
                        uniqueEntityId = gameDataManager.getUniqueEntityId()
                        runtimeEntityId = gameDataManager.getRuntimeEntityId()
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            val interactionSource2 = remember { MutableInteractionSource() }
            val isPressed2 by interactionSource2.collectIsPressedAsState()

            val animatedScale2 by animateFloatAsState(
                targetValue = if (isPressed2) 0.98f else 1f,
                animationSpec = tween(200),
                label = "scale2"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) 
                    .scale(animatedScale2)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                if (isPressed2) Color(0xFF10B981) else Color(0xFF374151),
                                if (isPressed2) Color(0xFF06B6D4) else Color(0xFF4B5563)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource2,
                        indication = null
                    ) { /* Handle network status click */ },
                colors = CardDefaults.cardColors(
                    containerColor = if (isPressed2) Color(0xFF1F1F23) else Color(0xFF18181B)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isPressed2) 8.dp else 6.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    if (hasData) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            modifier = Modifier.size(20.dp),
                            shape = CircleShape,
                            color = if (hasData) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                            shadowElevation = 1.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (hasData) ir.alirezaivaz.tablericons.R.drawable.ic_wifi
                                        else ir.alirezaivaz.tablericons.R.drawable.ic_wifi_off
                                    ),
                                    contentDescription = "Network Status",
                                    tint = if (hasData) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (hasData) "Connected" else "Disconnected",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hasData) "Game data available" else "No game data",
                                color = Color(0xFFA1A1AA),
                                fontSize = 8.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }

            
            val interactionSource1 = remember { MutableInteractionSource() }
            val isPressed1 by interactionSource1.collectIsPressedAsState()

            val animatedScale1 by animateFloatAsState(
                targetValue = if (isPressed1) 0.98f else 1f,
                animationSpec = tween(200),
                label = "scale1"
            )

            val animatedBorderColor by animateColorAsState(
                targetValue = if (isPressed1) KitsuPrimary else Color(0xFF374151),
                animationSpec = tween(200),
                label = "borderColor1"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .scale(animatedScale1)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                animatedBorderColor,
                                animatedBorderColor.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource1,
                        indication = null
                    ) { WorldStatsOverlay.showOverlay()},
                colors = CardDefaults.cardColors(
                    containerColor = if (isPressed1) Color(0xFF1F1F23) else Color(0xFF18181B)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isPressed1) 8.dp else 6.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2A2A2E).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = KitsuPrimary.copy(alpha = 0.2f),
                                shadowElevation = 2.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_universe),
                                        contentDescription = "Version Icon",
                                        tint = KitsuPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Game Version",
                                    color = Color(0xFFA1A1AA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when {
                                        isLoading -> "Loading..."
                                        !hasData -> "No Data"
                                        vanillaVersion.isNullOrEmpty() -> "Unknown"
                                        else -> vanillaVersion!!
                                    },
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_device_gamepad_2),
                                    contentDescription = "Game Mode",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = gameMode ?: "Unknown Mode",
                                    color = Color(0xFFE5E7EB),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_id),
                                    contentDescription = "Entity ID",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "ID: ${uniqueEntityId ?: "N/A"}",
                                    color = Color(0xFFE5E7EB),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (defaultPlayerPermission != null && defaultPlayerPermission != PlayerPermission.VISITOR)
                                            ir.alirezaivaz.tablericons.R.drawable.ic_check
                                        else
                                            ir.alirezaivaz.tablericons.R.drawable.ic_x
                                    ),
                                    contentDescription = "Default Permissions",
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (defaultPlayerPermission != null && defaultPlayerPermission != PlayerPermission.VISITOR) "Perms: Enabled" else "Perms: Disabled",
                                    color = Color(0xFFE5E7EB),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_command),
                                    contentDescription = "Command",
                                    tint = Color(0xFF06B6D4),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Commands: ${commandsEnabled ?: "N/A"}",
                                    color = Color(0xFFE5E7EB),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val interactionSource3 = remember { MutableInteractionSource() }
            val isPressed3 by interactionSource3.collectIsPressedAsState()

            val animatedScale3 by animateFloatAsState(
                targetValue = if (isPressed3) 0.98f else 1f,
                animationSpec = tween(150),
                label = "scale3"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .scale(animatedScale3)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                if (isPressed3) KitsuPrimary else Color(0xFF374151),
                                if (isPressed3) Color(0xFF06B6D4) else Color(0xFF4B5563)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource3,
                        indication = null
                    ) { /* Handle player list click */ },
                colors = CardDefaults.cardColors(
                    containerColor = if (isPressed3) Color(0xFF1F1F23) else Color(0xFF18181B)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isPressed3) 8.dp else 6.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                ) {
                    PlayerListUI()
                }
            }

            val interactionSource4 = remember { MutableInteractionSource() }
            val isPressed4 by interactionSource4.collectIsPressedAsState()

            val animatedScale4 by animateFloatAsState(
                targetValue = if (isPressed4) 0.98f else 1f,
                animationSpec = tween(200),
                label = "scale4"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .scale(animatedScale4)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = if (isPressed4)
                                listOf(Color(0xFF8B5CF6).copy(alpha = 0.6f), Color(0xFF06B6D4).copy(alpha = 0.6f))
                            else
                                listOf(Color(0xFF374151), Color(0xFF4B5563))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource4,
                        indication = null
                    ) { /* Handle level info click */ },
                colors = CardDefaults.cardColors(
                    containerColor = if (isPressed4) Color(0xFF1F1F23) else Color(0xFF18181B)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isPressed4) 8.dp else 6.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(18.dp),
                        shape = CircleShape,
                        color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = ir.alirezaivaz.tablericons.R.drawable.ic_world),
                                contentDescription = "Level Icon",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Current Level",
                            color = Color(0xFFA1A1AA),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when {
                                isLoading -> "Loading..."
                                !hasData -> "No Level Data"
                                levelName.isNullOrEmpty() -> "Unknown Level"
                                else -> levelName!!
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}