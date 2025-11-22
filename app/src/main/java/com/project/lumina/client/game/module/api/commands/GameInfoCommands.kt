package com.project.lumina.client.game.module.api.commands

import com.project.lumina.client.constructors.GameDataManager
import com.project.lumina.client.constructors.GameManager
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import java.util.UUID

class GameInfoCommands(
    private val cmdListener: CmdListener,
    private val moduleManager: GameManager
) {

    companion object {
        const val HEADER_COLOR = "§6"
        const val ACCENT_COLOR = "§e"
        const val SUCCESS_COLOR = "§a"
        const val ERROR_COLOR = "§c"
        const val INFO_COLOR = "§7"
        const val VALUE_COLOR = "§b"
    }

    private fun sendClientMessage(message: String) {
        cmdListener.sendClientMessage(message)
    }

    private fun GameManager.getGameDataManager(): GameDataManager {
        return this.netBound?.gameDataManager ?: throw IllegalStateException("NetBound not initialized")
    }

    fun handleSeed() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}World seed not available.")
            return
        }

        val seed = netBound.getWorldSeed()
        if (seed != null) {
            sendClientMessage("${HEADER_COLOR}World Seed ${INFO_COLOR}▼")
            sendClientMessage("${ACCENT_COLOR}Seed: $VALUE_COLOR$seed")
        } else {
            sendClientMessage("${ERROR_COLOR}Failed to retrieve world seed.")
        }
    }

    fun handlePlayerList() {
        val netBound = moduleManager.netBound
        if (netBound == null) {
            sendClientMessage("${ERROR_COLOR}Player list not available.")
            return
        }

        val players = netBound.getCurrentPlayers()
        if (players.isEmpty()) {
            sendClientMessage("${ERROR_COLOR}No players online.")
            return
        }

        sendClientMessage("${HEADER_COLOR}Player List ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}Total Players: $VALUE_COLOR${players.size}")
        players.forEach { player ->
            val roles = mutableListOf<String>()
            if (player.isHost) roles.add("Host")
            if (player.isTeacher) roles.add("Teacher")
            if (player.isSubClient) roles.add("SubClient")
            val roleStr = if (roles.isNotEmpty()) " [$INFO_COLOR${roles.joinToString(", ")}$ACCENT_COLOR]" else ""
            sendClientMessage("$INFO_COLOR- $ACCENT_COLOR${player.name}$roleStr")
        }
    }

    fun handleSpawn() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}Spawn coordinates not available.")
            return
        }

        val spawn = netBound.gameDataManager.getWorldSpawn()
        if (spawn != null) {
            sendClientMessage("${HEADER_COLOR}World Spawn ${INFO_COLOR}▼")
            sendClientMessage("${ACCENT_COLOR}Coordinates: $VALUE_COLOR${spawn.x}, ${spawn.y}, ${spawn.z}")
        } else {
            sendClientMessage("${ERROR_COLOR}Failed to retrieve spawn coordinates.")
        }
    }

    fun handleGameRules() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}Game rules not available.")
            return
        }

        val gameRules = netBound.gameDataManager.getGamerules()
        if (gameRules != null && gameRules.isNotEmpty()) {
            sendClientMessage("${HEADER_COLOR}Game Rules ${INFO_COLOR}▼")
            gameRules.forEach { rule ->
                sendClientMessage("$INFO_COLOR- $ACCENT_COLOR$rule")
            }
        } else {
            sendClientMessage("${ERROR_COLOR}No game rules available.")
        }
    }

    fun handleIds() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}Entity IDs not available.")
            return
        }

        val runtimeId = netBound.gameDataManager.getRuntimeEntityId()
        val uniqueId = netBound.gameDataManager.getUniqueEntityId()
        sendClientMessage("${HEADER_COLOR}Entity IDs ${INFO_COLOR}▼")
        if (runtimeId != null) {
            sendClientMessage("${ACCENT_COLOR}Runtime ID: $VALUE_COLOR$runtimeId")
        } else {
            sendClientMessage("${ERROR_COLOR}Runtime ID not available.")
        }
        if (uniqueId != null) {
            sendClientMessage("${ACCENT_COLOR}Unique Entity ID: $VALUE_COLOR$uniqueId")
        } else {
            sendClientMessage("${ERROR_COLOR}Unique Entity ID not available.")
        }
    }

    fun handleWorldInfo() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}World information not available.")
            return
        }

        val gameData = netBound.gameDataManager
        sendClientMessage("${HEADER_COLOR}World Info ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}World Name: $VALUE_COLOR${gameData.getWorldName() ?: "N/A"}")
        sendClientMessage("${ACCENT_COLOR}Level Name: $VALUE_COLOR${gameData.getLevelName() ?: "N/A"}")
        sendClientMessage("${ACCENT_COLOR}Dimension ID: $VALUE_COLOR${gameData.getDimensionId() ?: "N/A"}")
        sendClientMessage("${ACCENT_COLOR}Difficulty: $VALUE_COLOR${gameData.getDifficulty() ?: "N/A"}")
        sendClientMessage("${ACCENT_COLOR}Game Mode: $VALUE_COLOR${gameData.getGameMode() ?: "N/A"}")
    }

    fun handleMinimap(args: List<String>) {
        val netBound = moduleManager.netBound
        if (netBound == null) {
            sendClientMessage("${ERROR_COLOR}Minimap controls not available.")
            return
        }

        if (args.isEmpty()) {
            sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!minimap [on/off/size/zoom/dot] [value]")
            return
        }

        when (args[0].lowercase()) {
            "on" -> {
                netBound.enableMinimap(true)
                sendClientMessage("${SUCCESS_COLOR}Minimap enabled.")
            }
            "off" -> {
                netBound.enableMinimap(false)
                sendClientMessage("${SUCCESS_COLOR}Minimap disabled.")
            }
            "size" -> {
                if (args.size < 2) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!minimap size <value>")
                    return
                }
                try {
                    val size = args[1].toFloat()
                    netBound.updateMinimapSize(size)
                    sendClientMessage("${SUCCESS_COLOR}Minimap size set to $VALUE_COLOR$size$INFO_COLOR.")
                } catch (e: Exception) {
                    sendClientMessage("${ERROR_COLOR}Invalid size value: ${e.message}")
                }
            }
            "zoom" -> {
                if (args.size < 2) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!minimap zoom <value>")
                    return
                }
                try {
                    val zoom = args[1].toFloat()
                    netBound.updateMinimapZoom(zoom)
                    sendClientMessage("${SUCCESS_COLOR}Minimap zoom set to $VALUE_COLOR$zoom$INFO_COLOR.")
                } catch (e: Exception) {
                    sendClientMessage("${ERROR_COLOR}Invalid zoom value: ${e.message}")
                }
            }
            "dot" -> {
                if (args.size < 2) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!minimap dot <value>")
                    return
                }
                try {
                    val dotSize = args[1].toInt()
                    netBound.updateDotSize(dotSize)
                    sendClientMessage("${SUCCESS_COLOR}Minimap dot size set to $VALUE_COLOR$dotSize$INFO_COLOR.")
                } catch (e: Exception) {
                    sendClientMessage("${ERROR_COLOR}Invalid dot size value: ${e.message}")
                }
            }
            else -> sendClientMessage("${ERROR_COLOR}Invalid minimap option. Use on/off/size/zoom/dot.")
        }
    }

    fun handleGameMode() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}Game mode information not available.")
            return
        }

        val gameData = netBound.gameDataManager
        sendClientMessage("${HEADER_COLOR}Game Mode ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}Player Game Mode: $VALUE_COLOR${gameData.getPlayerGameType() ?: "N/A"}")
        sendClientMessage("${ACCENT_COLOR}Level Game Mode: $VALUE_COLOR${gameData.getLevelGameType() ?: "N/A"}")
    }

    fun handleCoords() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}Player coordinates not available.")
            return
        }

        val position = netBound.gameDataManager.getPlayerPosition()
        if (position != null) {
            sendClientMessage("${HEADER_COLOR}Player Coordinates ${INFO_COLOR}▼")
            sendClientMessage("${ACCENT_COLOR}Position: $VALUE_COLOR${position.x}, ${position.y}, ${position.z}")
        } else {
            sendClientMessage("${ERROR_COLOR}Failed to retrieve player coordinates.")
        }
    }

    fun handleTime() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}Time information not available.")
            return
        }

        val gameData = netBound.gameDataManager
        val dayCycleStopTime = gameData.getDayCycleStopTime()
        val currentTick = gameData.getCurrentTick()
        sendClientMessage("${HEADER_COLOR}Game Time ${INFO_COLOR}▼")
        if (dayCycleStopTime != null) {
            sendClientMessage("${ACCENT_COLOR}Day Cycle Stop Time: $VALUE_COLOR$dayCycleStopTime")
        } else {
            sendClientMessage("${ACCENT_COLOR}Day Cycle Stop Time: $ERROR_COLOR}N/A")
        }
        if (currentTick != null) {
            sendClientMessage("${ACCENT_COLOR}Current Tick: $VALUE_COLOR$currentTick")
        } else {
            sendClientMessage("${ACCENT_COLOR}Current Tick: $ERROR_COLOR}N/A")
        }
    }

    fun handleBiome() {
        val netBound = moduleManager.netBound
        if (netBound == null || !netBound.hasStartGameData()) {
            sendClientMessage("${ERROR_COLOR}Biome information not available.")
            return
        }

        val gameData = netBound.gameDataManager
        sendClientMessage("${HEADER_COLOR}Spawn Biome ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}Biome Type: $VALUE_COLOR${gameData.getSpawnBiomeType() ?: "N/A"}")
        sendClientMessage("${ACCENT_COLOR}Custom Biome Name: $VALUE_COLOR${gameData.getCustomBiomeName() ?: "N/A"}")
    }

    fun handlePlayers(args: List<String>) {
        val netBound = moduleManager.netBound
        if (netBound == null) {
            sendClientMessage("${ERROR_COLOR}Player information not available.")
            return
        }

        if (args.isEmpty()) {
            sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!players [name/uuid/entityid] <value>")
            return
        }

        when (args[0].lowercase()) {
            "name" -> {
                if (args.size < 2) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!players name <player_name>")
                    return
                }
                val player = netBound.getPlayerByName(args[1])
                if (player != null) {
                    displayPlayerInfo(player)
                } else {
                    sendClientMessage("${ERROR_COLOR}Player '$ACCENT_COLOR${args[1]}$ERROR_COLOR' not found.")
                }
            }
            "uuid" -> {
                if (args.size < 2) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!players uuid <uuid>")
                    return
                }
                try {
                    val uuid = UUID.fromString(args[1])
                    val player = netBound.getPlayerByUUID(uuid)
                    if (player != null) {
                        displayPlayerInfo(player)
                    } else {
                        sendClientMessage("${ERROR_COLOR}Player with UUID '$ACCENT_COLOR${args[1]}$ERROR_COLOR' not found.")
                    }
                } catch (e: IllegalArgumentException) {
                    sendClientMessage("${ERROR_COLOR}Invalid UUID format.")
                }
            }
            "entityid" -> {
                if (args.size < 2) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!players entityid <id>")
                    return
                }
                try {
                    val entityId = args[1].toLong()
                    val player = netBound.getPlayerByEntityId(entityId)
                    if (player != null) {
                        displayPlayerInfo(player)
                    } else {
                        sendClientMessage("${ERROR_COLOR}Player with Entity ID '$ACCENT_COLOR${args[1]}$ERROR_COLOR' not found.")
                    }
                } catch (e: NumberFormatException) {
                    sendClientMessage("${ERROR_COLOR}Invalid Entity ID format.")
                }
            }
            else -> sendClientMessage("${ERROR_COLOR}Invalid option. Use name/uuid/entityid.")
        }
    }

    private fun displayPlayerInfo(player: GameDataManager.PlayerInfo) {
        sendClientMessage("${HEADER_COLOR}Player Info ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}Name: $VALUE_COLOR${player.name}")
        sendClientMessage("${ACCENT_COLOR}UUID: $VALUE_COLOR${player.uuid}")
        sendClientMessage("${ACCENT_COLOR}Entity ID: $VALUE_COLOR${player.entityId}")
        sendClientMessage("${ACCENT_COLOR}XUID: $VALUE_COLOR${player.xuid}")
        sendClientMessage("${ACCENT_COLOR}Platform Chat ID: $VALUE_COLOR${player.platformChatId}")
        sendClientMessage("${ACCENT_COLOR}Build Platform: $VALUE_COLOR${player.buildPlatform}")
        val roles = mutableListOf<String>()
        if (player.isHost) roles.add("Host")
        if (player.isTeacher) roles.add("Teacher")
        if (player.isSubClient) roles.add("SubClient")
        val roleStr = if (roles.isNotEmpty()) roles.joinToString(", ") else "None"
        sendClientMessage("${ACCENT_COLOR}Roles: $VALUE_COLOR$roleStr")
        sendClientMessage("${ACCENT_COLOR}Trusted Skin: $VALUE_COLOR${player.hasTrustedSkin}")
    }

    fun handleOverlay(args: List<String>) {
        val netBound = moduleManager.netBound
        if (netBound == null) {
            sendClientMessage("${ERROR_COLOR}Overlay controls not available.")
            return
        }

        if (args.isEmpty()) {
            sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!overlay [arraylist/speedometer/keystrokes] [on/off]")
            return
        }

        when (args[0].lowercase()) {
            "arraylist" -> {
                if (args.size < 2 || args[1].lowercase() in listOf("on", "off")) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!overlay arraylist [on/off]")
                    return
                }
                val enable = args[1].lowercase() == "on"
                netBound.enableArrayList(enable)
                sendClientMessage("${SUCCESS_COLOR}ArrayList overlay ${if (enable) "enabled" else "disabled"}.")
            }
            "speedometer" -> {
                if (args.size < 2 || args[1].lowercase() in listOf("on", "off")) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!overlay speedometer [on/off]")
                    return
                }
                val enable = args[1].lowercase() == "on"
                if (enable) {
                    val position = netBound.gameDataManager.getPlayerPosition() ?: return sendClientMessage("${ERROR_COLOR}Player position not available.")
                    netBound.showSpeedometer(position)
                    sendClientMessage("${SUCCESS_COLOR}Speedometer overlay enabled.")
                } else {
                    
                    sendClientMessage("${ERROR_COLOR}Disabling speedometer not supported yet.")
                }
            }
            "keystrokes" -> {
                if (args.size < 2 || args[1].lowercase() in listOf("on", "off")) {
                    sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!overlay keystrokes [on/off]")
                    return
                }
                
                sendClientMessage("${ERROR_COLOR}Keystrokes overlay toggle not supported yet.")
            }
            else -> sendClientMessage("${ERROR_COLOR}Invalid overlay option. Use arraylist/speedometer/keystrokes.")
        }
    }

    fun handleStats() {
        val netBound = moduleManager.netBound
        if (netBound == null) {
            sendClientMessage("${ERROR_COLOR}Game data stats not available.")
            return
        }

        val stats = netBound.gameDataManager.getDataStats()
        sendClientMessage("${HEADER_COLOR}Game Data Stats ${INFO_COLOR}▼")
        sendClientMessage("$INFO_COLOR$stats")
    }

    fun handleVersion() {
        val netBound = moduleManager.netBound
        if (netBound == null) {
            sendClientMessage("${ERROR_COLOR}Version information not available.")
            return
        }

        val gameData = netBound.gameDataManager
        sendClientMessage("${HEADER_COLOR}Version Info ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}Game Version: $VALUE_COLOR${gameData.getVanillaVersion() ?: "N/A"}")
        sendClientMessage("${ACCENT_COLOR}Protocol Version: $VALUE_COLOR${netBound.protocolVersion}")
    }
}