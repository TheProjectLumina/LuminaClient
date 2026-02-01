package com.project.lumina.client.game.module.impl.combat

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import com.project.lumina.client.util.AssetManager

class DamageBoostElement(iconResId: Int = AssetManager.getAsset("ic_border_outer")) : Element(
    name = "DamageBoost",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_damageboost_display_name")
) {
    private val speedSetting = 1.0f
    private val upwardsMultiplier = 1.0f
    private val adjustForKnockback = false
    private val applyUpwardsVelocity = false

    private var lastYaw: Float = 0.0f
    private var isPlayerMoving: Boolean = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
            lastYaw = packet.rotation.y
            isPlayerMoving = packet.motion.x != 0.0f || packet.motion.y != 0.0f
            return
        }

        if (packet is SetEntityMotionPacket && packet.runtimeEntityId == session.localPlayer.runtimeEntityId) {
            if (!isPlayerMoving) return

            val appliedVelocity = packet.motion
            val appliedSpeed = sqrt(
                appliedVelocity.x * appliedVelocity.x + appliedVelocity.z * appliedVelocity.z
            ) * 10.0f

            val yawRadians = Math.toRadians(lastYaw.toDouble())
            val x = -sin(yawRadians).toFloat()
            val z = cos(yawRadians).toFloat()

            var damageBoost = speedSetting / 10.0f
            if (adjustForKnockback && appliedSpeed > 1.0f) {
                damageBoost += appliedSpeed / 10.0f
            }

            var vely = 0.0f
            if (applyUpwardsVelocity) {
                vely = appliedVelocity.y
            }
            vely *= upwardsMultiplier

            packet.motion = Vector3f.from(
                x * damageBoost,
                vely,
                z * damageBoost
            )
        }
    }
}