package com.project.lumina.client.game.module.impl.combat

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.entity.*
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import com.project.lumina.client.util.AssetManager

class HitboxElement(iconResId: Int = AssetManager.getAsset("ic_box")) : Element(
    name = "Hitbox",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_hitbox_display_name")
) {

    private val hitboxWidth by floatValue("Width", 1.5f, 0.5f..8f)
    private val hitboxHeight by floatValue("Height", 1.5f, 0.5f..8f)
    private var playersOnly by boolValue("Players", true)
    private var mobsOnly by boolValue("Mobs", false)
    private val particleCount =  8
    private val visualizeHitbox = false
    private var lastParticleTime = 0L
    private val particleInterval = 500L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (session.localPlayer.tickExists % 40 == 0L) {
                session.level.entityMap.values.forEach { entity ->
                    if (entity.isTarget()) {
                        val metadata = EntityDataMap()
                        metadata.put(EntityDataTypes.WIDTH, hitboxWidth)
                        metadata.put(EntityDataTypes.HEIGHT, hitboxHeight)
                        metadata.put(EntityDataTypes.SCALE, 1.0f)

                        session.clientBound(SetEntityDataPacket().apply {
                            runtimeEntityId = entity.runtimeEntityId
                            this.metadata = metadata
                        })

                        if (visualizeHitbox && currentTime - lastParticleTime >= particleInterval) {
                            visualizeHitboxWithParticles(entity)
                            lastParticleTime = currentTime
                        }
                    }
                }
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            session.level.entityMap.values.forEach { entity ->
                if (entity.isTarget()) {
                    val metadata = EntityDataMap()
                    metadata.put(EntityDataTypes.WIDTH, 0.6f)
                    metadata.put(EntityDataTypes.HEIGHT, 1.8f)
                    metadata.put(EntityDataTypes.SCALE, 1.0f)

                    session.clientBound(SetEntityDataPacket().apply {
                        runtimeEntityId = entity.runtimeEntityId
                        this.metadata = metadata
                    })
                }
            }
        }
    }

    private fun visualizeHitboxWithParticles(entity: Entity) {
        val pos = entity.vec3Position
        val radius = hitboxWidth / 2f

        repeat(particleCount) { i ->
            val angle = (i * (360.0 / particleCount)).toDouble()
            val x = pos.x + radius * Math.cos(Math.toRadians(angle))
            val z = pos.z + radius * Math.sin(Math.toRadians(angle))

            session.clientBound(EntityEventPacket().apply {
                runtimeEntityId = entity.runtimeEntityId
                type = EntityEventType.LOVE_PARTICLES
                data = 0
            })
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (playersOnly && !mobsOnly) {
                    !this.isBot()
                } else if (playersOnly && mobsOnly) {
                    !this.isBot()
                } else {
                    false
                }
            }
            is EntityUnknown -> {
                if (mobsOnly && !playersOnly) {
                    isMob()
                } else if (mobsOnly && playersOnly) {
                    isMob()
                } else {
                    false
                }
            }
            else -> false
        }
    }
}