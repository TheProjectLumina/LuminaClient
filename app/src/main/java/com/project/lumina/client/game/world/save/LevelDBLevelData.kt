package com.project.lumina.client.game.world.save

import com.project.lumina.client.game.world.save.LevelDBGameRules
import com.project.lumina.client.game.world.save.LevelDBPlayerAbilities
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.nbt.NBTOutputStream
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.nbt.NbtType
import org.cloudburstmc.nbt.util.stream.LittleEndianDataOutputStream
import java.io.ByteArrayOutputStream

class LevelDBLevelData(var protocol: Int, var inventoryVersion: String) {
    
    var isCommandsEnabled = true
    var currentTick = 0L
    var hasBeenLoadedInCreative = false
    var hasLockedResourcePack = false
    var hasLockedBehaviorPack = false
    var experiments = NbtMap.EMPTY
    var isForcedGamemode = false
    var isImmutable = false
    var isConfirmedPlatformLockedContent = false
    var isFromWorldTemplate = false
    var isFromLockedTemplate = false
    var isMultiplayerGame = false
    var isSingleUseWorld = false
    var isWorldTemplateOptionsLocked = false
    var isLanBroadcast = false
    var isLanBroadcastIntent = false
    var isMultiplayerGameIntent = false
    var platformBroadcastIntent = 0
    var requiresCopiedPackRemovalCheck = false
    var serverChunkTickRange = 4
    var spawnOnlyV1Villagers = false
    var storageVersion = 8
    var isTexturePacksRequired = false
    var useMsaGamerTagsOnly = false
    var name = "Bedrock Level"
    var worldStartCount = 0L
    var xboxLiveBroadcastIntent = 0

    
    var eduOffer = 0
    var isEduEnabled = false

    
    var biomeOverride = ""
    var isBonusChestEnabled = false
    var isBonusChestSpawned = false
    var isCenterMapsToOrigin = false
    var defaultGamemode = 0
    var difficulty = 1
    var flatWorldLayers = ""
    var lightningLevel = 0f
    var lightningTime = 0
    var limitedWorldCoordinates = Vector3i.from(0, 32767, 0)
    var limitedWorldWidth = 16
    var netherScale = 8
    var rainLevel = 0f
    var rainTime = 0
    var seed = 0L
    var worldSpawn = Vector3i.from(128, 64, 128)
    var startWithMapEnabled = false
    var time = 0L
    var worldType = 1 

    
    var baseGameVersion = "*"
    var lastPlayed = System.currentTimeMillis()
    var minimumCompatibleClientVersion = intArrayOf(1, 18, 0, 0, 0)
    var lastOpenedWithVersion = intArrayOf(1, 18, 0, 0, 0)
    var platform = 2
    var prid = ""

	var playerAbilities = LevelDBPlayerAbilities()
	var gameRules = LevelDBGameRules()

	fun toNbtMap(): NbtMap {
		return gameRules.toNbtMap().toBuilder()
			.putCompound("abilities", playerAbilities.toNbtMap())
			.putBoolean("commandsEnabled", isCommandsEnabled)
			.putLong("currentTick", currentTick)
			.putBoolean("hasBeenLoadedInCreative", hasBeenLoadedInCreative)
			.putBoolean("hasLockedResourcePack", hasLockedResourcePack)
			.putBoolean("hasLockedBehaviorPack", hasLockedBehaviorPack)
			.putCompound("experiments", experiments)
			.putBoolean("ForceGameType", isForcedGamemode)
			.putBoolean("immutableWorld", isImmutable)
			.putBoolean("ConfirmedPlatformLockedContent", isConfirmedPlatformLockedContent)
			.putBoolean("isFromWorldTemplate", isFromWorldTemplate)
			.putBoolean("isFromLockedTemplate", isFromLockedTemplate)
			.putBoolean("MultiplayerGame", isMultiplayerGame)
			.putBoolean("isSingleUseWorld", isSingleUseWorld)
			.putBoolean("isWorldTemplateOptionLocked", isWorldTemplateOptionsLocked)
			.putBoolean("LANBroadcast", isLanBroadcast)
			.putBoolean("LANBroadcastIntent", isLanBroadcastIntent)
			.putBoolean("MultiplayerGameIntent", isMultiplayerGameIntent)
			.putInt("PlatformBroadcastIntent", platformBroadcastIntent)
			.putBoolean("requiresCopiedPackRemovalCheck", requiresCopiedPackRemovalCheck)
			.putInt("serverChunkTickRange", serverChunkTickRange)
			.putBoolean("SpawnV1Villagers", spawnOnlyV1Villagers)
			.putInt("StorageVersion", storageVersion)
			.putBoolean("texturePacksRequired", isTexturePacksRequired)
			.putBoolean("useMsaGamertagsOnly", useMsaGamerTagsOnly)
			.putString("LevelName", name)
			.putLong("worldStartCount", worldStartCount)
			.putInt("XBLBroadcastIntent", xboxLiveBroadcastIntent)
			.putInt("eduOffer", eduOffer)
			.putBoolean("educationFeaturesEnabled", isEduEnabled)
			.putString("BiomeOverride", biomeOverride)
			.putBoolean("bonusChestEnabled", isBonusChestEnabled)
			.putBoolean("bonusChestSpawned", isBonusChestSpawned)
			.putBoolean("CenterMapsToOrigin", isCenterMapsToOrigin)
			.putInt("GameType", defaultGamemode)
			.putInt("Difficulty", difficulty)
			.putString("FlatWorldLayers", flatWorldLayers)
			.putFloat("lightningLevel", lightningLevel)
			.putInt("lightningTime", lightningTime)
			.putInt("LimitedWorldOriginX", limitedWorldCoordinates.x)
			.putInt("LimitedWorldOriginY", limitedWorldCoordinates.y)
			.putInt("LimitedWorldOriginZ", limitedWorldCoordinates.z)
			.putInt("limitedWorldWidth", limitedWorldWidth)
			.putInt("NetherScale", netherScale)
			.putFloat("rainLevel", rainLevel)
			.putInt("rainTime", rainTime)
			.putLong("RandomSeed", seed)
			.putInt("SpawnX", worldSpawn.x)
			.putInt("SpawnY", worldSpawn.y)
			.putInt("SpawnZ", worldSpawn.z)
			.putBoolean("startWithMapEnabled", startWithMapEnabled)
			.putLong("Time", time)
			.putInt("Generator", worldType)
			.putString("baseGameVersion", baseGameVersion)
			.putString("InventoryVersion", inventoryVersion)
			.putLong("LastPlayed", lastPlayed)
			.putList("MinimumCompatibleClientVersion", NbtType.INT, minimumCompatibleClientVersion.map { it })
			.putList("lastOpenedWithVersion", NbtType.INT, minimumCompatibleClientVersion.map { it })
			.putInt("Platform", platform)
			.putInt("NetworkVersion", protocol)
			.putString("prid", prid)
			.build()
	}

	fun toBytes(): ByteArray {
		val map = toNbtMap()
		val os = ByteArrayOutputStream()
		NBTOutputStream(LittleEndianDataOutputStream(os)).apply {
			writeTag(map)
			close()
		}
		return os.toByteArray()
	}
}
