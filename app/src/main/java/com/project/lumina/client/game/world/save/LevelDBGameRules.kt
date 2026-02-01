package com.project.lumina.client.game.world.save

import org.cloudburstmc.nbt.NbtMap

class LevelDBGameRules {
	var commandBlockOutput = true
	var commandBlocksEnabled = true
	var doDaylightCycle = true
	var doEntityDrops = true
	var doFireTick = true
	var doImmediateRespawn = false
	var doInsomnia = true
	var doMobLoot = true
	var doMobSpawning = true
	var doTileDrops = true
	var doWeatherCycle = true
	var drowningDamage = true
	var fallDamage = true
	var fireDamage = true
	var freezeDamage = true
	var keepInventory = false
	var maxCommandChainLength = 65535
	var mobGriefing = true
	var naturalRegeneration = true
	var pvp = true
	var randomTickSpeed = 1
	var sendCommandFeedback = true
	var showCoordinates = true
	var showDeathMessages = true
	var showTags = true
	var spawnRadius = 5
	var tntExplodes = true

	fun toNbtMap(): NbtMap {
		return NbtMap.builder()
			.putBoolean("commandblockoutput", commandBlockOutput)
			.putBoolean("commandblocksenabled", commandBlocksEnabled)
			.putBoolean("dodaylightcycle", doDaylightCycle)
			.putBoolean("doentitydrops", doEntityDrops)
			.putBoolean("dofiretick", doFireTick)
			.putBoolean("doimmediaterespawn", doImmediateRespawn)
			.putBoolean("doinsomnia", doInsomnia)
			.putBoolean("domobloot", doMobLoot)
			.putBoolean("domobspawning", doMobSpawning)
			.putBoolean("dotiledrops", doTileDrops)
			.putBoolean("doweathercycle", doWeatherCycle)
			.putBoolean("drowningdamage", drowningDamage)
			.putBoolean("falldamage", fallDamage)
			.putBoolean("firedamage", fireDamage)
			.putBoolean("freezedamage", freezeDamage)
			.putBoolean("keepinventory", keepInventory)
			.putInt("maxcommandchainlength", maxCommandChainLength)
			.putBoolean("mobgriefing", mobGriefing)
			.putBoolean("naturalregeneration", naturalRegeneration)
			.putBoolean("pvp", pvp)
			.putInt("randomtickspeed", randomTickSpeed)
			.putBoolean("sendcommandfeedback", sendCommandFeedback)
			.putBoolean("showcoordinates", showCoordinates)
			.putBoolean("showdeathmessages", showDeathMessages)
			.putBoolean("showtags", showTags)
			.putInt("spawnradius", spawnRadius)
			.putBoolean("tntexplodes", tntExplodes)
			.build()
	}
}
