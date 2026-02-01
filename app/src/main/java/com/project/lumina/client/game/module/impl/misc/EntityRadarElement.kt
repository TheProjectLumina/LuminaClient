package com.project.lumina.client.game.module.impl

import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.EntityStorage
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.overlay.entityradar.EntityRadarOverlay
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.mods.EntityRadarShortcutButton
import com.project.lumina.client.util.AssetManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.GameType
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class EntityRadarElement(
    iconResId: Int = AssetManager.getAsset("ic_area2_black_24dp")
) : Element(
    name = "EntityRadar",
    category = CheatCategory.Visual,
    iconResId,
    displayNameResId = AssetManager.getString("module_entity_radar_display_name")
) {

    private var radarOverlay: EntityRadarOverlay? = null
    private var shortcutButton: EntityRadarShortcutButton? = null
    private var isFollowingEntity = false
    private var targetEntity: EntityStorage.EntityInfo? = null

    private var followDistance = 2.0f
    private var followSpeed = 3.0f
    private var isSpectating = false
    private var spectateTarget: EntityStorage.EntityInfo? = null
    private var originalPlayerPosition: Vector3f? = null
    private var originalPlayerRotation: Vector3f? = null
    private var originalGameType: GameType? = null
    private var isAttacking = false
    private var attackTarget: EntityStorage.EntityInfo? = null
    private var lastAttackTime = 0L

    
    private var isDesynced = false
    private val storedPackets = ConcurrentLinkedQueue<PlayerAuthInputPacket>()
    private val updateDelay = 1000L
    private val minResendInterval = 100L
    private val maxResendInterval = 300L

    override fun onEnabled() {
        super.onEnabled()
        if (!isSessionCreated) {
            isEnabled = false
            return
        }
        showShortcut()
    }

    override fun onDisabled() {
        super.onDisabled()
        hideShortcut()
        hideRadar()
        stopFollowing()
        if (isSpectating) {
            stopSpectating()
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        
        if (isDesynced && interceptablePacket.packet is PlayerAuthInputPacket) {
            storedPackets.add(interceptablePacket.packet as PlayerAuthInputPacket)
            interceptablePacket.intercept()
            return
        }

        if (interceptablePacket.packet !is PlayerAuthInputPacket) return

        if (isFollowingEntity) {
            val target = targetEntity ?: return
            val playerPos = session.localPlayer.vec3Position

            val actualEntity = session.level.entityMap[target.id] ?: return
            val targetPos = actualEntity.vec3Position

            val distance = targetPos.distance(playerPos)

            if (distance > followDistance) {
                val direction = atan2(
                    targetPos.z - playerPos.z,
                    targetPos.x - playerPos.x
                ) - Math.toRadians(180.0).toFloat()

                val newPos = Vector3f.from(
                    playerPos.x - sin(direction) * followSpeed,
                    targetPos.y.coerceIn(playerPos.y - 0.5f, playerPos.y + 0.5f),
                    playerPos.z + cos(direction) * followSpeed
                )

                val yaw = atan2(
                    targetPos.z - playerPos.z,
                    targetPos.x - playerPos.x
                ).toFloat() + Math.toRadians(90.0).toFloat()

                val pitch = -atan2(
                    targetPos.y - playerPos.y,
                    Vector3f.from(targetPos.x, playerPos.y, targetPos.z).distance(playerPos)
                ).toFloat()

                session.clientBound(MovePlayerPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    position = newPos
                    rotation = Vector3f.from(pitch, yaw, yaw)
                    mode = MovePlayerPacket.Mode.NORMAL
                    onGround = true
                    tick = session.localPlayer.tickExists
                })
            }
        }

        if (isSpectating) {
            val target = spectateTarget ?: return
            val currentEntity = session.entityStorage.getEntities()[target.id]
            if (currentEntity != null) {
                updateSpectatePosition(currentEntity)
            }
        }

        if (isAttacking) {
            val target = attackTarget ?: return
            val currentEntity = session.entityStorage.getEntities()[target.id]
            if (currentEntity != null) {
                performAttack(currentEntity)
            } else {
                stopAttacking()
            }
        }
    }

    private fun showShortcut() {
        if (!isSessionCreated) return

        if (shortcutButton == null) {
            shortcutButton = EntityRadarShortcutButton(
                onShortcutClick = {
                    showRadar()
                }
            )
            OverlayManager.showOverlayWindow(shortcutButton!!)
        }
    }

    private fun hideShortcut() {
        shortcutButton?.let {
            OverlayManager.dismissOverlayWindow(it)
            shortcutButton = null
        }
    }

    private fun showRadar() {
        if (!isSessionCreated) return

        if (radarOverlay == null) {
            radarOverlay = EntityRadarOverlay(
                entityStorage = session.entityStorage,
                session = session,
                onFollowEntity = { entity ->
                    startFollowing(entity)
                },
                onStopFollowing = {
                    stopFollowing()
                },
                onSpectateEntity = { entity ->
                    spectateEntity(entity)
                },
                onDismiss = {
                    radarOverlay = null
                }
            )
            OverlayManager.showOverlayWindow(radarOverlay!!)
        }
    }

    private fun hideRadar() {
        radarOverlay?.let {
            OverlayManager.dismissOverlayWindow(it)
            radarOverlay = null
        }
    }

    private fun startFollowing(entity: EntityStorage.EntityInfo) {
        if (!isSessionCreated) return

        isFollowingEntity = true
        targetEntity = entity
        session.showNotification(
            "Following ${entity.name}",
            "Distance: ${followDistance} blocks",
            AssetManager.getAsset("moon_stars_24")
        )
    }

    fun attackEntity(entity: EntityStorage.EntityInfo) {
        if (!isSessionCreated) return

        if (isAttacking && attackTarget?.id == entity.id) {
            stopAttacking()
        } else {
            startAttacking(entity)
        }
    }

    private fun startAttacking(entity: EntityStorage.EntityInfo) {
        isAttacking = true
        attackTarget = entity
        session.showNotification(
            "Auto-Attacking ${entity.name}",
            "Attacking continuously",
            AssetManager.getAsset("sword_24")
        )
    }

    private fun stopAttacking() {
        if (isAttacking) {
            isAttacking = false
            val entityName = attackTarget?.name ?: "entity"
            attackTarget = null

            if (isSessionCreated) {
                session.showNotification(
                    "Stopped Attacking",
                    "No longer attacking $entityName",
                    AssetManager.getAsset("cross_circle_24")
                )
            }
        }
    }

    private fun performAttack(entity: EntityStorage.EntityInfo) {
        if (!isSessionCreated) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAttackTime < 500) return
        lastAttackTime = currentTime

        val playerPos = session.localPlayer.vec3Position
        val dx = entity.coords.x - playerPos.x
        val dy = entity.coords.y - playerPos.y
        val dz = entity.coords.z - playerPos.z
        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        if (distance <= 7.0f) {
            try {
                val packet = InventoryTransactionPacket()
                packet.transactionType = InventoryTransactionType.ITEM_USE_ON_ENTITY
                packet.runtimeEntityId = entity.id
                packet.actionType = 1
                session.clientBound(packet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun followEntity(entity: EntityStorage.EntityInfo) {
        startFollowing(entity)
    }

    fun isAttackingEntity(entityId: Long): Boolean {
        return isAttacking && attackTarget?.id == entityId
    }

    fun isSpectatingEntity(entityId: Long): Boolean {
        return isSpectating && spectateTarget?.id == entityId
    }

    fun spectateEntity(entity: EntityStorage.EntityInfo) {
        if (!isSessionCreated) return

        if (isSpectating && spectateTarget?.id == entity.id) {
            stopSpectating()
        } else {
            startSpectating(entity)
        }
    }

    private fun startSpectating(entity: EntityStorage.EntityInfo) {
        if (!isSessionCreated) return

        
        originalPlayerPosition = session.localPlayer.vec3Position
        originalPlayerRotation = Vector3f.from(0f, 0f, 0f)
        originalGameType = GameType.SURVIVAL 

        
        isDesynced = true
        storedPackets.clear()

        isSpectating = true
        spectateTarget = entity

        try {
            
            val spectatorPacket = SetPlayerGameTypePacket().apply {
                gamemode = GameType.SPECTATOR.ordinal
            }
            session.clientBound(spectatorPacket)

            
            val currentEntity = session.entityStorage.getEntities()[entity.id]
            if (currentEntity != null) {
                updateSpectatePosition(currentEntity)
            } else {
                
                val targetPos = entity.coords
                val offsetPos = Vector3f.from(
                    targetPos.x,
                    targetPos.y + 2.0f,
                    targetPos.z - 5.0f
                )

                val yaw = atan2(
                    targetPos.z - offsetPos.z,
                    targetPos.x - offsetPos.x
                ).toFloat() + Math.toRadians(90.0).toFloat()

                val pitch = -atan2(
                    targetPos.y - offsetPos.y,
                    Vector3f.from(targetPos.x, offsetPos.y, targetPos.z).distance(offsetPos)
                ).toFloat()

                session.clientBound(MovePlayerPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    position = offsetPos
                    rotation = Vector3f.from(pitch, yaw, yaw)
                    mode = MovePlayerPacket.Mode.NORMAL
                    onGround = false
                    tick = session.localPlayer.tickExists
                })
            }

            session.showNotification(
                "Spectating ${entity.name}",
                "Spectator mode + Desync active",
                AssetManager.getAsset("eye_24")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            stopSpectating()
        }
    }

    private fun updateSpectatePosition(entityInfo: EntityStorage.EntityInfo) {
        try {
            val targetPos = entityInfo.coords
            val offsetPos = Vector3f.from(
                targetPos.x,
                targetPos.y + 2.0f,
                targetPos.z - 5.0f
            )

            val yaw = atan2(
                targetPos.z - offsetPos.z,
                targetPos.x - offsetPos.x
            ).toFloat() + Math.toRadians(90.0).toFloat()

            val pitch = -atan2(
                targetPos.y - offsetPos.y,
                Vector3f.from(targetPos.x, offsetPos.y, targetPos.z).distance(offsetPos)
            ).toFloat()

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                position = offsetPos
                rotation = Vector3f.from(pitch, yaw, yaw)
                mode = MovePlayerPacket.Mode.NORMAL
                onGround = false
                tick = session.localPlayer.tickExists
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun stopSpectating() {
        if (!isSessionCreated || !isSpectating) return

        isSpectating = false
        val entityName = spectateTarget?.name ?: "entity"
        spectateTarget = null

        try {
            
            val restoreGameTypePacket = SetPlayerGameTypePacket().apply {
                gamemode = (originalGameType ?: GameType.SURVIVAL).ordinal
            }
            session.clientBound(restoreGameTypePacket)

            
            if (originalPlayerPosition != null && originalPlayerRotation != null) {
                session.clientBound(MovePlayerPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    position = originalPlayerPosition
                    rotation = originalPlayerRotation
                    mode = MovePlayerPacket.Mode.NORMAL
                    onGround = true
                    tick = session.localPlayer.tickExists
                })
            }

            
            isDesynced = false
            GlobalScope.launch {
                delay(updateDelay)
                while (storedPackets.isNotEmpty()) {
                    val packet = storedPackets.poll()
                    if (packet != null) {
                        session.clientBound(packet)
                    }
                    delay(Random.nextLong(minResendInterval, maxResendInterval))
                }
            }

            session.showNotification(
                "Stopped Spectating",
                "Returned to normal mode",
                AssetManager.getAsset("cross_circle_24")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            originalPlayerPosition = null
            originalPlayerRotation = null
            originalGameType = null
        }
    }

    private fun stopFollowing() {
        if (isFollowingEntity) {
            isFollowingEntity = false
            val entityName = targetEntity?.name ?: "entity"
            targetEntity = null

            if (isSessionCreated) {
                session.showNotification(
                    "Stopped Following",
                    "No longer following $entityName",
                    AssetManager.getAsset("cross_circle_24")
                )
            }
        }
    }

    private fun Vector3f.distance(other: Vector3f): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}