package com.project.lumina.client.game.entity

import android.util.Log
import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.game.inventory.AbstractInventory
import com.project.lumina.client.game.inventory.ContainerInventory
import com.project.lumina.client.game.inventory.PlayerInventory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode
import org.cloudburstmc.protocol.bedrock.data.SoundEvent
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.*

import java.util.UUID

@Suppress("MemberVisibilityCanBePrivate")
class LocalPlayer(val session: NetBound) : Player(0L, 0L, UUID.randomUUID(), "") {

    override var runtimeEntityId: Long = 0L
        private set

    override var uniqueEntityId: Long = 0L
        private set

    override var uuid: UUID = UUID.randomUUID()
        private set

    var blockBreakServerAuthoritative = false
        private set

    var movementServerAuthoritative = true
        private set

    var inventoriesServerAuthoritative = false
        private set

    var soundServerAuthoritative = false
        private set

    override val inventory = PlayerInventory(this)

    var openContainer: AbstractInventory? = null
        private set

    var isOnGround: Boolean = false
        private set

    override fun onPacketBound(packet: BedrockPacket) {
        super.onPacketBound(packet)

        when (packet) {
            is StartGamePacket -> {
                runtimeEntityId = packet.runtimeEntityId
                uniqueEntityId = packet.uniqueEntityId

                movementServerAuthoritative =
                    packet.authoritativeMovementMode != AuthoritativeMovementMode.CLIENT
                packet.authoritativeMovementMode = AuthoritativeMovementMode.SERVER
                inventoriesServerAuthoritative = packet.isInventoriesServerAuthoritative
                blockBreakServerAuthoritative = packet.isServerAuthoritativeBlockBreaking
                soundServerAuthoritative = packet.networkPermissions.isServerAuthSounds

                reset()
            }

            is PlayerAuthInputPacket -> {
                move(packet.position)
                rotate(packet.rotation)
                tickExists = packet.tick

                
                isOnGround = packet.motion.y == 0f
            }

            is MovePlayerPacket -> {
                isOnGround = packet.onGround
            }

            is ContainerOpenPacket -> {
                openContainer = if (packet.id.toInt() == 0) {
                    return
                } else {
                    ContainerInventory(packet.id.toInt(), packet.type)
                }
            }

            is ContainerClosePacket -> {
                if (packet.id.toInt() == openContainer?.containerId) {
                    openContainer = null
                }
            }
        }

        inventory.onPacketBound(packet)
        openContainer?.also {
            if (it is ContainerInventory) {
                it.onPacketBound(packet)
            }
        }
    }

    fun swing() {
        val animatePacket = AnimatePacket().apply {
            action = AnimatePacket.Action.SWING_ARM
            runtimeEntityId = this@LocalPlayer.runtimeEntityId
        }

       
        session.clientBound(animatePacket)

        val levelSoundEventPacket = LevelSoundEventPacket().apply {
            sound = SoundEvent.ATTACK_NODAMAGE
            position = vec3Position
            extraData = -1
            identifier = "minecraft:player"
            isBabySound = false
            isRelativeVolumeDisabled = false
        }

        //session.serverBound(levelSoundEventPacket)
        session.clientBound(levelSoundEventPacket)
    }

    fun attack(entity: Entity) {
        swing()







        val inventoryTransactionPacket = InventoryTransactionPacket().apply {
            transactionType = InventoryTransactionType.ITEM_USE_ON_ENTITY
            actionType = 1
            runtimeEntityId = entity.runtimeEntityId
            hotbarSlot = inventory.heldItemSlot
            itemInHand = inventory.hand
            playerPosition = vec3Position
            clickPosition = Vector3f.ZERO
        }

        session.serverBound(inventoryTransactionPacket)
    }

    fun setInventoriesServerAuthoritative(value: Boolean) {
        inventoriesServerAuthoritative = value
    }

    override fun onDisconnect() {
        super.onDisconnect()
        reset()
    }
}
