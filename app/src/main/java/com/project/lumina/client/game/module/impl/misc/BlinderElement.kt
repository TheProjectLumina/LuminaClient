package com.project.lumina.client.game.module.impl.misc

import android.util.Log
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.util.AssetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class BlinderElement : Element(
    name = "Blinder",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_blinder_display_name")
) {
    
    private val rangeValue by intValue("Range", 3, 1..10)
    private val intervalValue by intValue("Interval", 50, 10..500)
    private val enableNotifications by boolValue("Notifications", true)
    
    private var blindJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastPlayerPosition: Vector3i? = null
    
    override fun onEnabled() {
        super.onEnabled()
        startBlinding()
        if (isSessionCreated && enableNotifications) {
            session.displayClientMessage("§a§lEnabled §eBlinder§a. Range: §e${rangeValue}")
        }
    }
    
    override fun onDisabled() {
        super.onDisabled()
        stopBlinding()
        if (isSessionCreated && enableNotifications) {
            session.displayClientMessage("§c§lDisabled §eBlinder§c.")
        }
    }
    
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket ) {
            val newPosition = Vector3i.from(
                packet.position.x.toInt(),
                packet.position.y.toInt(),
                packet.position.z.toInt()
            )

            if (lastPlayerPosition != newPosition) {
                lastPlayerPosition = newPosition
                if (blindJob?.isActive != true) {
                    startBlinding()
                }
            }
        }
    }
    
    private fun startBlinding() {
        stopBlinding()
        blindJob = scope.launch {
            while (isEnabled) {
                try {
                    sendBlindPackets()
                    delay(intervalValue.toLong())
                } catch (e: kotlinx.coroutines.CancellationException) {
                    Log.d("BlinderElement", "Blinding coroutine cancelled (normal behavior)")
                    break
                } catch (e: Exception) {
                    Log.e("BlinderElement", "Unexpected error during blinding: ${e.javaClass.simpleName} - ${e.message}")
                    if (enableNotifications && isSessionCreated) {
                        session.displayClientMessage("§c[Blinder] §7Error occurred, retrying...")
                    }
                    delay(1000)
                }
            }
        }
    }
    
    private fun stopBlinding() {
        blindJob?.cancel()
        blindJob = null
    }
    
    private fun sendBlindPackets() {
        val playerPos = lastPlayerPosition ?: run {
            Log.w("BlinderElement", "Cannot send blind packets: player position is null")
            return
        }

        if (!isSessionCreated) {
            Log.w("BlinderElement", "Cannot send blind packets: session not created")
            return
        }

        val px = playerPos.x
        val py = playerPos.y
        val pz = playerPos.z
        val size = rangeValue
        var packetsSent = 0
        var packetsFailedToSend = 0

        for (dx in 0..size) {
            for (dy in 0..size) {
                for (dz in 0..size) {
                    val blockX = px + dx - 2
                    val blockY = py + dy - 1
                    val blockZ = pz + dz - 2

                    val blockPosition = Vector3i.from(blockX, blockY, blockZ)
                    val resultPosition = Vector3i.from(blockX, blockY, blockZ)

                    try {
                        val playerActionPacket = PlayerActionPacket().apply {
                            runtimeEntityId = session.localPlayer.runtimeEntityId
                            action = PlayerActionType.BUILD_DENIED
                            this.blockPosition = blockPosition
                            this.resultPosition = resultPosition
                            face = 0
                        }

                        session.serverBound(playerActionPacket)
                        packetsSent++
                    } catch (e: Exception) {
                        packetsFailedToSend++
                        Log.e("BlinderElement", "Error sending blind packet at ($blockX, $blockY, $blockZ): ${e.javaClass.simpleName} - ${e.message}")
                    }
                }
            }
        }

        if (packetsFailedToSend > 0) {
            Log.w("BlinderElement", "Blinding cycle completed: $packetsSent sent, $packetsFailedToSend failed")
        } else {
            Log.d("BlinderElement", "Blinding cycle completed successfully: $packetsSent packets sent")
        }
    }
    
    fun getCurrentRange(): Int = rangeValue
    
    fun getCurrentInterval(): Int = intervalValue
    
    fun isBlinding(): Boolean = blindJob?.isActive == true
}