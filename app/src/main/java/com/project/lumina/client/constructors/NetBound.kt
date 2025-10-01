package com.project.lumina.client.constructors

import android.util.Log
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.event.EventManager
import com.project.lumina.client.game.event.EventPacketInbound
import com.project.lumina.client.game.registry.BlockMapping
import com.project.lumina.client.game.registry.BlockMappingProvider
import com.project.lumina.client.game.world.Level
import com.project.lumina.client.game.world.World
import com.project.lumina.client.overlay.mods.MiniMapOverlay
import com.project.lumina.client.overlay.mods.OverlayModuleList
import com.project.lumina.client.overlay.mods.PacketNotificationOverlay
import com.project.lumina.client.overlay.mods.Position
import com.project.lumina.client.overlay.mods.SessionStatsOverlay
import com.project.lumina.client.overlay.mods.SpeedometerOverlay
import com.project.lumina.relay.LuminaRelaySession
import com.project.lumina.client.game.registry.ItemMapping
import com.project.lumina.client.game.registry.ItemMappingProvider
import com.project.lumina.client.game.registry.LegacyBlockMapping
import com.project.lumina.client.game.registry.LegacyBlockMappingProvider
import com.project.lumina.client.overlay.mods.KeystrokesOverlay
import com.project.lumina.client.overlay.mods.TargetHudOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.bedrock.packet.ItemComponentPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket
import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket
import org.cloudburstmc.protocol.bedrock.packet.BookEditPacket
import org.cloudburstmc.protocol.bedrock.packet.CommandRequestPacket
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry
import java.util.Collections
import java.util.UUID

@Suppress("MemberVisibilityCanBePrivate")
class NetBound(val luminaRelaySession: LuminaRelaySession) : ComposedPacketHandler, com.project.lumina.client.game.event.Listenable {

    override val eventManager = EventManager()

    val world = World(this)
    val level = Level(this)
    val localPlayer = LocalPlayer(this)

    private val proxyPlayerNames: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())

    
    val gameDataManager = GameDataManager()

    val protocolVersion: Int
        get() = luminaRelaySession.server.codec.protocolVersion

    private val mappingProviderContext = AppContext.instance

    private val blockMappingProvider = BlockMappingProvider(mappingProviderContext)
    private val itemMappingProvider = ItemMappingProvider(mappingProviderContext)
    private val legacyBlockMappingProvider = LegacyBlockMappingProvider(mappingProviderContext)

    lateinit var blockMapping: BlockMapping
    lateinit var itemMapping: ItemMapping
    lateinit var legacyBlockMapping: LegacyBlockMapping

    private var startGameReceived = false
    private val pendingPackets = mutableListOf<BedrockPacket>()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    private var playerPosition = Position(0f, 0f)
    private var playerRotation = 0f
    private val entityPositions = mutableMapOf<Long, Position>()
    private var minimapEnabled = false
    private var minimapUpdateScheduled = false
    private var minimapSize = 100f
    private var minimapZoom = 1.0f
    private var minimapDotSize = 5
    private var tracersEnabled = false

    private val versionName by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppContext.instance.packageManager.getPackageInfo(
            AppContext.instance.packageName, 0
        ).versionName
    }

    fun clientBound(packet: BedrockPacket) {
        luminaRelaySession.clientBound(packet)
    }

    fun serverBound(packet: BedrockPacket) {
        luminaRelaySession.serverBound(packet)
    }




    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        GameManager.setNetBound(this)

        if (packet is TextPacket) {
            if (packet.type == TextPacket.Type.CHAT) {
                proxyPlayerNames.add(packet.sourceName)
            }


        }

        if (packet is BookEditPacket) {
            if (packet.text.toString().length > 5000) {
                return true
            }
        }

        if (packet is CommandRequestPacket) {
            if (packet.command.length > 1000) {
                return true
            }
        }

        when (packet) {
            is StartGamePacket -> {
                gameDataManager.storeStartGamePacket(packet)

                try {
                    val itemDefinitions = SimpleDefinitionRegistry.builder<ItemDefinition>()
                        .addAll(packet.itemDefinitions)
                        .build()

                    luminaRelaySession.server.peer.codecHelper.itemDefinitions = itemDefinitions
                    luminaRelaySession.client?.peer?.codecHelper?.itemDefinitions = itemDefinitions

                    Log.i("NetBound", "Successfully set up codecHelper itemDefinitions: ${packet.itemDefinitions.size} items")
                } catch (e: Exception) {
                    Log.e("NetBound", "Failed to set up codecHelper itemDefinitions", e)
                }

                if (!startGameReceived) {
                    startGameReceived = true
                    Log.e("StartGamePacket", packet.toString())
                    Log.i("GameSession", "Seed: ${gameDataManager.getSeed()}")
                    Log.i("GameSession", "Game Mode: ${gameDataManager.getGameMode()}")
                    Log.i("GameSession", "LevelName: ${gameDataManager.getLevelName()}")

                    /**
                    try {
                    blockMapping = blockMappingProvider.craftMapping(protocolVersion)
                    itemMapping = itemMappingProvider.craftMapping(protocolVersion)
                    legacyBlockMapping = legacyBlockMappingProvider.craftMapping(protocolVersion)

                    Log.i("GameSession", "Loaded mappings for protocol $protocolVersion")
                    } catch (e: Exception) {
                    Log.e("GameSession", "Failed to load mappings for protocol $protocolVersion", e)
                    }
                     */
                }
            }

            is ItemComponentPacket -> {
                
                try {
                    val itemDefinitions = SimpleDefinitionRegistry.builder<ItemDefinition>()
                        .addAll(packet.items)
                        .build()

                    luminaRelaySession.server.peer.codecHelper.itemDefinitions = itemDefinitions
                    luminaRelaySession.client?.peer?.codecHelper?.itemDefinitions = itemDefinitions

                    Log.i("NetBound", "Successfully updated codecHelper from ItemComponentPacket: ${packet.items.size} items")
                } catch (e: Exception) {
                    Log.e("NetBound", "Failed to update codecHelper from ItemComponentPacket", e)
                }
            }

            is PlayerListPacket -> {
                gameDataManager.handlePlayerListPacket(packet)
            }





        }

        localPlayer.onPacketBound(packet)
        world.onPacket(packet)
        level.onPacketBound(packet)

        val event = EventPacketInbound(this, packet)
        eventManager.emit(event)

        if (event.isCanceled()) return true

        val interceptablePacket = InterceptablePacket(packet)
        for (module in GameManager.elements) {
            module.beforePacketBound(interceptablePacket)
            if (interceptablePacket.isIntercepted) return true
        }

        displayClientMessage("[Lumina V4]", TextPacket.Type.TIP)

        return false
    }

    override fun afterPacketBound(packet: BedrockPacket) {
        for (module in GameManager.elements) {
            module.afterPacketBound(packet)
        }
    }

    override fun onDisconnect(reason: String) {
        localPlayer.onDisconnect()
        level.onDisconnect()
        proxyPlayerNames.clear()
        GameManager.clearNetBound()
        gameDataManager.clearAllData()
        startGameReceived = false

        for (module in GameManager.elements) {
            module.onDisconnect(reason)
        }
    }

    fun getStartGameData(): Map<String, Any?> = gameDataManager.getStartGameData()
    fun getStartGameField(fieldName: String): Any? = gameDataManager.getStartGameField(fieldName)
    fun hasStartGameData(): Boolean = gameDataManager.hasStartGameData()
    fun getWorldName(): String? = gameDataManager.getWorldName()
    fun getPlayerSpawnPosition(): Vector3f? = gameDataManager.getPlayerPosition()
    fun getWorldSeed(): Long? = gameDataManager.getSeed()
    fun getLevelId(): String? = gameDataManager.getLevelId()
    fun getPacketData(packetType: String): Map<String, Any?> = gameDataManager.getPacketData(packetType)
    fun getPacketField(packetType: String, fieldName: String): Any? = gameDataManager.getPacketField(packetType, fieldName)
    fun getGameDataStats(): String = gameDataManager.getDataStats()
    fun getCurrentPlayers(): List<GameDataManager.PlayerInfo> = gameDataManager.getCurrentPlayerList()
    fun getPlayerByName(name: String): GameDataManager.PlayerInfo? = gameDataManager.getPlayerByName(name)
    fun getPlayerByUUID(uuid: UUID): GameDataManager.PlayerInfo? = gameDataManager.getPlayerByUUID(uuid)
    fun getPlayerByEntityId(entityId: Long): GameDataManager.PlayerInfo? = gameDataManager.getPlayerByEntityId(entityId)
    fun getPlayerCount(): Int = gameDataManager.getPlayerCount()
    fun getPlayerListStats(): String = gameDataManager.getPlayerListStats()
    fun getPlayersWithRole(): Map<String, List<GameDataManager.PlayerInfo>> = gameDataManager.getPlayersWithRole()
    fun isPlayerOnline(playerName: String): Boolean = getPlayerByName(playerName) != null
    fun isPlayerOnlineByUUID(uuid: UUID): Boolean = getPlayerByUUID(uuid) != null
    fun getHosts(): List<GameDataManager.PlayerInfo> = getPlayersWithRole()["hosts"] ?: emptyList()
    fun getTeachers(): List<GameDataManager.PlayerInfo> = getPlayersWithRole()["teachers"] ?: emptyList()
    fun logCurrentPlayerList() {
        Log.i("NetBound", "=== Current Player List ===")
        Log.i("NetBound", getPlayerListStats())
        Log.i("NetBound", "========================")
    }


    fun displayClientMessage(message: String, type: TextPacket.Type = TextPacket.Type.RAW) {
        val textPacket = TextPacket()
        textPacket.type = type
        textPacket.sourceName = ""
        textPacket.message = message
        textPacket.xuid = ""
        textPacket.platformChatId = ""
        textPacket.filteredMessage = ""
        clientBound(textPacket)
    }

    fun launchOnMain(block: suspend CoroutineScope.() -> Unit) {
        mainScope.launch {
            block()
        }
    }

    suspend fun showSessionStatsOverlay(initialStats: List<String>): SessionStatsOverlay =
        withContext(Dispatchers.Main) {
            try {
                val overlay = SessionStatsOverlay.showSessionStats(initialStats)
                overlay
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

    fun showNotification(title: String, subtitle: String, ResId: Int) {
        mainScope.launch {
            try {
                PacketNotificationOverlay.showNotification(
                    title = title,
                    subtitle = subtitle,
                    iconRes = ResId,
                    duration = 1000L
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showSpeedometer(position: Vector3f) {
        mainScope.launch {
            try {
                SpeedometerOverlay.showOverlay()
                SpeedometerOverlay.updatePosition(position)
            } catch (e: Exception) {
            }
        }
    }

    fun updatePlayerPosition(x: Float, z: Float) {
        playerPosition = Position(x, z)
        if (minimapEnabled) {
            scheduleMinimapUpdate()
        }
    }

    fun updatePlayerRotation(yaw: Float) {
        playerRotation = yaw
        if (minimapEnabled) {
            scheduleMinimapUpdate()
        }
    }

    fun updateEntityPosition(entityId: Long, x: Float, z: Float) {
        entityPositions[entityId] = Position(x, z)
        if (minimapEnabled && !minimapUpdateScheduled) {
            scheduleMinimapUpdate()
        }
    }

    fun updateMinimapSize(size: Float) {
        minimapSize = size
        if (minimapEnabled) {
            mainScope.launch {
                MiniMapOverlay.setMinimapSize(size)
                updateMinimap()
            }
        }
    }

    fun updateMinimapZoom(zoom: Float) {
        minimapZoom = zoom
        if (minimapEnabled) {
            mainScope.launch {
                MiniMapOverlay.overlayInstance.minimapZoom = zoom
                updateMinimap()
            }
        }
    }

    fun updateDotSize(dotSize: Int) {
        minimapDotSize = dotSize
        if (minimapEnabled) {
            mainScope.launch {
                MiniMapOverlay.overlayInstance.minimapDotSize = dotSize
                updateMinimap()
            }
        }
    }

    private fun scheduleMinimapUpdate() {
        if (!minimapUpdateScheduled) {
            minimapUpdateScheduled = true
            mainScope.launch {
                updateMinimap()
                minimapUpdateScheduled = false
            }
        }
    }

    fun enableMinimap(enable: Boolean) {
        if (enable != minimapEnabled) {
            minimapEnabled = enable
            if (enable) {
                mainScope.launch {
                    MiniMapOverlay.setOverlayEnabled(true)
                    MiniMapOverlay.setMinimapSize(minimapSize)
                    updateMinimap()
                }
            } else {
                mainScope.launch {
                    MiniMapOverlay.setOverlayEnabled(false)
                }
            }
        }
    }

    private fun updateMinimap() {
        try {
            MiniMapOverlay.setCenter(playerPosition.x, playerPosition.y)
            MiniMapOverlay.setPlayerRotation(playerRotation)

            MiniMapOverlay.overlayInstance.minimapZoom = minimapZoom
            MiniMapOverlay.overlayInstance.minimapDotSize = minimapDotSize

            val targets = entityPositions.values.toList()

            val finalTargets = if (targets.isEmpty()) {
                targets
            } else {
                targets
            }

            MiniMapOverlay.setTargets(finalTargets)
            MiniMapOverlay.showOverlay()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearEntityPositions() {
        entityPositions.clear()
        if (minimapEnabled) {
            scheduleMinimapUpdate()
        }
    }

    fun showMinimap(centerX: Float, centerZ: Float, targets: List<Position>) {
        mainScope.launch {
            MiniMapOverlay.setOverlayEnabled(true)
            MiniMapOverlay.setMinimapSize(minimapSize)
            MiniMapOverlay.setCenter(centerX, centerZ)
            MiniMapOverlay.setTargets(targets)
            MiniMapOverlay.showOverlay()

            minimapEnabled = true
            playerPosition = Position(centerX, centerZ)
        }
    }

    fun enableArrayList(boolean: Boolean) {
        OverlayModuleList.setOverlayEnabled(enabled = boolean)
    }

    fun setArrayListMode(boolean: Boolean) {

    }

    fun arrayListUi(string: String) {

    }

    fun isProxyPlayer(playerName: String): Boolean {
        return proxyPlayerNames.contains(playerName)
    }

    fun toggleSounds(bool: Boolean) {
        ArrayListManager.setSoundEnabled(bool)
    }

    fun soundList(set: ArrayListManager.SoundSet) {
        ArrayListManager.setCurrentSoundSet(set)
    }

    fun keyPress(key: String, pressed: Boolean) {
        KeystrokesOverlay.setKeyState(key, pressed)
    }

    fun targetHud(user: String, distance: Float, maxdistance: Float, hurtTime: Float) {
        mainScope.launch {
            TargetHudOverlay.showTargetHud(user, null, distance, maxdistance, hurtTime)
        }
    }
}