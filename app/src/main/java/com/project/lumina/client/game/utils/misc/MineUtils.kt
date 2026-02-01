package com.project.lumina.client.game.utils.misc

import com.google.gson.JsonParser
import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.game.utils.constants.Effect
import com.project.lumina.client.game.registry.BlockDefinition
import com.project.lumina.client.game.registry.MappingProvider
import com.project.lumina.client.game.utils.constants.Enchantment
import com.project.lumina.client.game.utils.math.toVector3iFloor
import org.cloudburstmc.math.vector.Vector3i
import kotlin.math.round

/**
 * Utility class for mining blocks
 */
object MineUtils {

	private val hardnessMap: Map<String, Float>

	init {
		val stream = MappingProvider::class.java.getResourceAsStream("/assets/mcpedata/block_hardness.json")
			?: error("Missing resource: /assets/mcpedata/block_hardness.json")

		val json = JsonParser.parseReader(stream.reader(Charsets.UTF_8)).asJsonObject

		val map = mutableMapOf<String, Float>()
		json.entrySet().forEach { (k, v) ->
			map[k] = v.asFloat
		}

		hardnessMap = map
	}


	/**
	 * @return ticks required to break the given block
	 */
	fun calculateBreakTime(session: NetBound, block: BlockDefinition): Int {
		var speedMultiplier = 1f

		
		session.localPlayer.getEffectById(Effect.HASTE)?.let {
			speedMultiplier *= 0.2f * it.amplifier + 1
		}

		
		session.localPlayer.getEffectById(Effect.MINING_FATIGUE)?.let {
			speedMultiplier *= when (it.amplifier) {
				0 -> 0.3f
				1 -> 0.09f
				2 -> 0.0027f
				else -> 0.00081f
			}
		}

		
		val pos: Vector3i = session.localPlayer.vec3PositionFeet.toVector3iFloor()
		val blockAtFeet = session.blockMapping.getDefinition(session.world.getBlockIdAt(pos))
		val blockAboveFeet = session.blockMapping.getDefinition(session.world.getBlockIdAt(pos.add(0, 1, 0)))

		if ((blockAtFeet.identifier.contains("water") || blockAboveFeet.identifier.contains("water"))
			&& !session.localPlayer.inventory.hand.hasEnchant(Enchantment.WATER_WORKER)
		) {
			speedMultiplier /= 5f
		}

		
		if (!session.localPlayer.isOnGround) {
			speedMultiplier /= 5f
		}

		
		val hardness = hardnessMap[block.identifier] ?: 1f
		var damage = speedMultiplier / hardness
		damage /= 100f

		if (damage > 1f) {
			return 0 
		}

		return round(1 / damage).toInt()
	}
}