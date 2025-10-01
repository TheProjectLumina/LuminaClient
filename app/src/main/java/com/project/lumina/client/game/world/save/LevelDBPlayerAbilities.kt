package com.project.lumina.client.game.world.save

import org.cloudburstmc.nbt.NbtMap

class LevelDBPlayerAbilities {
    var canAttackMobs = true
    var canAttackPlayers = true
    var canBuild = true
    var canFly = false
    var canInstaBuild = false
    var canMine = true
    var canOpenContainers = true
    var canTeleport = false
    var canUseDoorsAndSwitches = true
    var flySpeed = 0.05f
    var isFlying = false
    var isInvulnerable = false
    var isOp = false
    var isLightning = false
    var permissionsLevel = 0
    var playerPermissionsLevel = 0
    var walkSpeed = 0.1f

	fun toNbtMap(): NbtMap {
		return NbtMap.builder()
			.putBoolean("attackmobs", canAttackMobs)
			.putBoolean("attackplayers", canAttackPlayers)
			.putBoolean("build", canBuild)
			.putBoolean("mayfly", canFly)
			.putBoolean("instabuild", canInstaBuild)
			.putBoolean("mine", canMine)
			.putBoolean("opencontainers", canOpenContainers)
			.putBoolean("teleport", canTeleport)
			.putBoolean("doorsandswitches", canUseDoorsAndSwitches)
			.putFloat("flySpeed", flySpeed)
			.putBoolean("flying", isFlying)
			.putBoolean("invulnerable", isInvulnerable)
			.putBoolean("op", isOp)
			.putBoolean("lightning", isLightning)
			.putInt("permissionsLevel", permissionsLevel)
			.putInt("playerPermissionsLevel", playerPermissionsLevel)
			.putFloat("walkSpeed", walkSpeed)
			.build()
	}
}
