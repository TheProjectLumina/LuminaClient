package com.project.lumina.client.constructors

import android.util.Log
import com.project.lumina.client.util.TextComponentUtil
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


class GameDataManager {

    private val packetDataStore: ConcurrentHashMap<String, MutableMap<String, Any?>> = ConcurrentHashMap()
    private val currentPlayerList: MutableMap<UUID, PlayerInfo> = Collections.synchronizedMap(mutableMapOf())

    data class PlayerInfo(
        val uuid: UUID,
        val entityId: Long,
        val name: String,
        val xuid: String,
        val platformChatId: String,
        val buildPlatform: Int,
        val isTeacher: Boolean,
        val isHost: Boolean,
        val hasTrustedSkin: Boolean,
        val isSubClient: Boolean,
        val color: Int,
        val addedTime: Long = System.currentTimeMillis()
    )

    fun handlePlayerListPacket(packet: PlayerListPacket) {
        storePacketData(packet, "PLAYER_LIST")

        when (packet.action) {
            PlayerListPacket.Action.ADD -> {
                packet.entries.forEach { entry ->
                    val playerInfo = PlayerInfo(
                        uuid = entry.uuid,
                        entityId = entry.entityId,
                        name = entry.name.toString(),
                        xuid = entry.xuid,
                        platformChatId = entry.platformChatId,
                        buildPlatform = entry.buildPlatform,
                        isTeacher = entry.isTeacher,
                        isHost = entry.isHost,
                        hasTrustedSkin = entry.isTrustedSkin,
                        isSubClient = entry.isSubClient,
                        color = entry.color
                    )

                    currentPlayerList[entry.uuid] = playerInfo
                    //logCurrentPlayerList()
                }
            }

            PlayerListPacket.Action.REMOVE -> {
                packet.entries.forEach { entry ->
                    currentPlayerList.remove(entry.uuid)
                }
            }
        }
    }

    
    fun clearPlayerList() {
        val clearedCount = currentPlayerList.size
        currentPlayerList.clear()
        Log.i("PlayerList", "Cleared player list ($clearedCount players)")
    }






    private fun logCurrentPlayerList() {
        if (currentPlayerList.isEmpty()) {
            Log.d("PlayerList", "   No players online")
            return
        }

        Log.d("PlayerList", "   Current players:")
        currentPlayerList.values.forEach { player ->
            Log.d("PlayerList", "   - ${player.name} (${player.uuid})")
        }
    }

    fun getCurrentPlayerList(): List<PlayerInfo> {
        return currentPlayerList.values.toList()
    }

    fun getPlayerByName(name: String): PlayerInfo? {
        return currentPlayerList.values.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getPlayerByUUID(uuid: UUID): PlayerInfo? {
        return currentPlayerList[uuid]
    }

    fun getPlayerByEntityId(entityId: Long): PlayerInfo? {
        return currentPlayerList.values.find { it.entityId == entityId }
    }

    fun getPlayerCount(): Int = currentPlayerList.size

    fun getPlayersWithRole(): Map<String, List<PlayerInfo>> {
        val players = currentPlayerList.values
        return mapOf(
            "hosts" to players.filter { it.isHost },
            "teachers" to players.filter { it.isTeacher },
            "subClients" to players.filter { it.isSubClient },
            "regular" to players.filter { !it.isHost && !it.isTeacher && !it.isSubClient }
        )
    }

    fun getPlayerListStats(): String {
        val stats = StringBuilder()
        stats.append("Player List Stats:\n")
        stats.append("  Total players: ${currentPlayerList.size}\n")

        if (currentPlayerList.isNotEmpty()) {
            val roleStats = getPlayersWithRole()
            stats.append("  Hosts: ${roleStats["hosts"]?.size ?: 0}\n")
            stats.append("  Teachers: ${roleStats["teachers"]?.size ?: 0}\n")
            stats.append("  Sub-clients: ${roleStats["subClients"]?.size ?: 0}\n")
            stats.append("  Regular players: ${roleStats["regular"]?.size ?: 0}\n")

            stats.append("  Players:\n")
            currentPlayerList.values.forEach { player ->
                val roles = mutableListOf<String>()
                if (player.isHost) roles.add("Host")
                if (player.isTeacher) roles.add("Teacher")
                if (player.isSubClient) roles.add("SubClient")
                val roleStr = if (roles.isNotEmpty()) " [${roles.joinToString(", ")}]" else ""

                stats.append("    - ${player.name}$roleStr\n")
            }
        }

        return stats.toString()
    }

    fun storePacketData(packet: BedrockPacket, packetTypeName: String? = null) {
        val typeName = packetTypeName ?: packet.javaClass.simpleName

        try {
            val packetData: MutableMap<String, Any?> = Collections.synchronizedMap(mutableMapOf())

            extractFieldsFromObject(packet, packetData)

            packetDataStore[typeName] = packetData
        } catch (e: Exception) {
            Log.i("Error Chan :3", "Crash Mommy 3000:" ,e)
        }
    }


    fun storeStartGamePacket(packet: StartGamePacket) {
        storePacketData(packet, "StartGame")
    }


    private fun extractFieldsFromObject(obj: Any, storage: MutableMap<String, Any?>) {
        var clazz: Class<*>? = obj.javaClass

        while (clazz != null && clazz != Any::class.java) {
            val fields = clazz.declaredFields

            for (field in fields) {
                try {
                    field.isAccessible = true
                    val fieldName = field.name
                    if (!java.lang.reflect.Modifier.isStatic(field.modifiers) &&
                        !field.isSynthetic &&
                        !storage.containsKey(fieldName)) {

                        val fieldValue = field.get(obj)
                        storage[fieldName] = fieldValue
                    }
                } catch (e: Exception) {
                    Log.w("GameDataManager", "Failed to access field ${field.name} in ${clazz.simpleName}: ${e.message}")
                }
            }

            clazz = clazz.superclass
        }
    }


    fun getPacketData(packetType: String): Map<String, Any?> {
        return packetDataStore[packetType]?.toMap() ?: emptyMap()
    }


    fun getPacketField(packetType: String, fieldName: String): Any? {
        return packetDataStore[packetType]?.get(fieldName)
    }


    fun hasPacketData(packetType: String): Boolean {
        return packetDataStore.containsKey(packetType) && packetDataStore[packetType]?.isNotEmpty() == true
    }

    fun getStoredPacketTypes(): Set<String> {
        return packetDataStore.keys.toSet()
    }


    fun clearPacketData(packetType: String) {
        packetDataStore.remove(packetType)
        Log.i("GameDataManager", "🧹 Cleared $packetType data")
    }

  fun clearAllData() {
        val clearedTypes = packetDataStore.keys.toList()
        packetDataStore.clear()
        clearPlayerList()
    }


    fun getDataStats(): String {
        val stats = StringBuilder()
        stats.append("GameDataManager Stats:\n")

        if (packetDataStore.isEmpty()) {
            stats.append("  No data stored")
        } else {
            packetDataStore.forEach { (type, data) ->
                stats.append("  $type: ${data.size} fields\n")
            }
        }

        return stats.toString()
    }

    fun getStartGameData(): Map<String, Any?> = getPacketData("StartGame")

    fun getStartGameField(fieldName: String): Any? = getPacketField("StartGame", fieldName)

    fun hasStartGameData(): Boolean = hasPacketData("StartGame")

    fun getWorldName(): String? = getStartGameField("worldName") as? String

    fun getLevelName(): String? = getStartGameField("levelName") as? String

    fun getLevelId(): String? = getStartGameField("levelId") as? String

    fun getGameMode(): org.cloudburstmc.protocol.bedrock.data.GameType? =
        getStartGameField("playerGameType") as? org.cloudburstmc.protocol.bedrock.data.GameType

    fun getVanillaVersion(): String? = getStartGameField("vanillaVersion") as? String

    fun getUniqueEntityId(): Long? = getStartGameField("uniqueEntityId") as? Long

    fun getRuntimeEntityId(): Long? = getStartGameField("runtimeEntityId") as? Long

    fun getPlayerGameType(): org.cloudburstmc.protocol.bedrock.data.GameType? =
        getStartGameField("playerGameType") as? org.cloudburstmc.protocol.bedrock.data.GameType

    fun getPlayerPosition(): org.cloudburstmc.math.vector.Vector3f? =
        getStartGameField("playerPosition") as? org.cloudburstmc.math.vector.Vector3f

    fun getRotation(): org.cloudburstmc.math.vector.Vector2f? =
        getStartGameField("rotation") as? org.cloudburstmc.math.vector.Vector2f

    
    fun getSeed(): Long? = getStartGameField("seed") as? Long

    fun getSpawnBiomeType(): org.cloudburstmc.protocol.bedrock.data.SpawnBiomeType? =
        getStartGameField("spawnBiomeType") as? org.cloudburstmc.protocol.bedrock.data.SpawnBiomeType

    fun getCustomBiomeName(): String? = getStartGameField("customBiomeName") as? String

    fun getDimensionId(): Int? = getStartGameField("dimensionId") as? Int

    fun getGeneratorId(): Int? = getStartGameField("generatorId") as? Int

    fun getLevelGameType(): org.cloudburstmc.protocol.bedrock.data.GameType? =
        getStartGameField("levelGameType") as? org.cloudburstmc.protocol.bedrock.data.GameType

    fun getDifficulty(): Int? = getStartGameField("difficulty") as? Int

    fun getDefaultSpawn(): org.cloudburstmc.math.vector.Vector3i? =
        getStartGameField("defaultSpawn") as? org.cloudburstmc.math.vector.Vector3i

    fun getAchievementsDisabled(): Boolean? = getStartGameField("achievementsDisabled") as? Boolean

    fun getDayCycleStopTime(): Int? = getStartGameField("dayCycleStopTime") as? Int

    fun getEduEditionOffers(): Int? = getStartGameField("eduEditionOffers") as? Int

    fun getEduFeaturesEnabled(): Boolean? = getStartGameField("eduFeaturesEnabled") as? Boolean

    fun getEducationProductionId(): String? = getStartGameField("educationProductionId") as? String

    fun getRainLevel(): Float? = getStartGameField("rainLevel") as? Float

    fun getLightningLevel(): Float? = getStartGameField("lightningLevel") as? Float

    fun getPlatformLockedContentConfirmed(): Boolean? = getStartGameField("platformLockedContentConfirmed") as? Boolean

    fun getMultiplayerGame(): Boolean? = getStartGameField("multiplayerGame") as? Boolean

    fun getBroadcastingToLan(): Boolean? = getStartGameField("broadcastingToLan") as? Boolean

    fun getXblBroadcastMode(): org.cloudburstmc.protocol.bedrock.data.GamePublishSetting? =
        getStartGameField("xblBroadcastMode") as? org.cloudburstmc.protocol.bedrock.data.GamePublishSetting

    fun getPlatformBroadcastMode(): org.cloudburstmc.protocol.bedrock.data.GamePublishSetting? =
        getStartGameField("platformBroadcastMode") as? org.cloudburstmc.protocol.bedrock.data.GamePublishSetting

    fun getCommandsEnabled(): Boolean? = getStartGameField("commandsEnabled") as? Boolean

    fun getTexturePacksRequired(): Boolean? = getStartGameField("texturePacksRequired") as? Boolean

    fun getExperiments(): List<*>? = getStartGameField("experiments") as? List<*>

    fun getExperimentsPreviouslyToggled(): Boolean? = getStartGameField("experimentsPreviouslyToggled") as? Boolean

    fun getBonusChestEnabled(): Boolean? = getStartGameField("bonusChestEnabled") as? Boolean

    fun getStartingWithMap(): Boolean? = getStartGameField("startingWithMap") as? Boolean

    fun getTrustingPlayers(): Boolean? = getStartGameField("trustingPlayers") as? Boolean


    fun getDefaultPlayerPermission(): org.cloudburstmc.protocol.bedrock.data.PlayerPermission? =
        getStartGameField("defaultPlayerPermission") as? org.cloudburstmc.protocol.bedrock.data.PlayerPermission

    fun getServerChunkTickRange(): Int? = getStartGameField("serverChunkTickRange") as? Int

    fun getBehaviorPackLocked(): Boolean? = getStartGameField("behaviorPackLocked") as? Boolean

    fun getResourcePackLocked(): Boolean? = getStartGameField("resourcePackLocked") as? Boolean

    fun getFromLockedWorldTemplate(): Boolean? = getStartGameField("fromLockedWorldTemplate") as? Boolean

    fun getUsingMsaGamertagsOnly(): Boolean? = getStartGameField("usingMsaGamertagsOnly") as? Boolean

    fun getFromWorldTemplate(): Boolean? = getStartGameField("fromWorldTemplate") as? Boolean

    fun getWorldTemplateOptionLocked(): Boolean? = getStartGameField("worldTemplateOptionLocked") as? Boolean

    fun getOnlySpawningV1Villagers(): Boolean? = getStartGameField("onlySpawningV1Villagers") as? Boolean

    fun getLimitedWorldWidth(): Int? = getStartGameField("limitedWorldWidth") as? Int

    fun getLimitedWorldHeight(): Int? = getStartGameField("limitedWorldHeight") as? Int

    fun getNetherType(): Boolean? = getStartGameField("netherType") as? Boolean

    fun getEduSharedUriResource(): org.cloudburstmc.protocol.bedrock.data.EduSharedUriResource? =
        getStartGameField("eduSharedUriResource") as? org.cloudburstmc.protocol.bedrock.data.EduSharedUriResource

    fun getForceExperimentalGameplay(): org.cloudburstmc.protocol.common.util.OptionalBoolean? =
        getStartGameField("forceExperimentalGameplay") as? org.cloudburstmc.protocol.common.util.OptionalBoolean

    fun getChatRestrictionLevel(): org.cloudburstmc.protocol.bedrock.data.ChatRestrictionLevel? =
        getStartGameField("chatRestrictionLevel") as? org.cloudburstmc.protocol.bedrock.data.ChatRestrictionLevel

    fun getDisablingPlayerInteractions(): Boolean? = getStartGameField("disablingPlayerInteractions") as? Boolean

    fun getDisablingPersonas(): Boolean? = getStartGameField("disablingPersonas") as? Boolean

    fun getDisablingCustomSkins(): Boolean? = getStartGameField("disablingCustomSkins") as? Boolean



    fun getPremiumWorldTemplateId(): String? = getStartGameField("premiumWorldTemplateId") as? String

    fun getTrial(): Boolean? = getStartGameField("trial") as? Boolean


    
    fun getAuthoritativeMovementMode(): org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode? =
        getStartGameField("authoritativeMovementMode") as? org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode

    fun getRewindHistorySize(): Int? = getStartGameField("rewindHistorySize") as? Int

    fun getServerAuthoritativeBlockBreaking(): Boolean? = getStartGameField("serverAuthoritativeBlockBreaking") as? Boolean

    
    fun getCurrentTick(): Long? = getStartGameField("currentTick") as? Long

    fun getEnchantmentSeed(): Int? = getStartGameField("enchantmentSeed") as? Int

    fun getBlockPalette(): org.cloudburstmc.nbt.NbtList<*>? =
        getStartGameField("blockPalette") as? org.cloudburstmc.nbt.NbtList<*>

    fun getBlockProperties(): List<*>? = getStartGameField("blockProperties") as? List<*>

    fun getItemDefinitions(): List<*>? = getStartGameField("itemDefinitions") as? List<*>

    fun getMultiplayerCorrelationId(): String? = getStartGameField("multiplayerCorrelationId") as? String

    fun getInventoriesServerAuthoritative(): Boolean? = getStartGameField("inventoriesServerAuthoritative") as? Boolean

    fun getPlayerPropertyData(): org.cloudburstmc.nbt.NbtMap? =
        getStartGameField("playerPropertyData") as? org.cloudburstmc.nbt.NbtMap

    fun getBlockRegistryChecksum(): Long? = getStartGameField("blockRegistryChecksum") as? Long

    fun getWorldTemplateId(): java.util.UUID? = getStartGameField("worldTemplateId") as? java.util.UUID

    fun getWorldEditor(): Boolean? = getStartGameField("worldEditor") as? Boolean

    fun getClientSideGenerationEnabled(): Boolean? = getStartGameField("clientSideGenerationEnabled") as? Boolean

    fun getEmoteChatMuted(): Boolean? = getStartGameField("emoteChatMuted") as? Boolean

    fun getBlockNetworkIdsHashed(): Boolean? = getStartGameField("blockNetworkIdsHashed") as? Boolean

    fun getCreatedInEditor(): Boolean? = getStartGameField("createdInEditor") as? Boolean

    fun getExportedFromEditor(): Boolean? = getStartGameField("exportedFromEditor") as? Boolean

    fun getNetworkPermissions(): org.cloudburstmc.protocol.bedrock.data.NetworkPermissions? =
        getStartGameField("networkPermissions") as? org.cloudburstmc.protocol.bedrock.data.NetworkPermissions

    fun getHardcore(): Boolean? = getStartGameField("hardcore") as? Boolean

    fun getServerId(): String? = getStartGameField("serverId") as? String

    fun getWorldId(): String? = getStartGameField("worldId") as? String

    fun getScenarioId(): String? = getStartGameField("scenarioId") as? String

    fun getGamerules(): List<*>? = getStartGameField("gamerules") as? List<*>


    fun getServerEngine(): String? = getStartGameField("serverEngine") as? String

    fun getWorldSpawn(): org.cloudburstmc.math.vector.Vector3i? = getStartGameField("defaultSpawn") as?
            org.cloudburstmc.math.vector.Vector3i


    companion object {
        const val START_GAME = "StartGame"
        const val PLAYER_LIST = "PlayerList"
        const val LEVEL_EVENT = "LevelEvent"
        const val SET_TIME = "SetTime"
        const val RESOURCE_PACK_INFO = "ResourcePackInfo"
    }
}