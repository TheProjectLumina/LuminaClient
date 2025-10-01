package com.project.lumina.client.game.module.impl.misc

import android.util.Log
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.entity.Player
import com.project.lumina.client.util.AssetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.random.Random

class SpamXPElement : Element(
    name = "SpamXP",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_spam_xp_display_name")
) {
    
    private val intervalValue by intValue("Interval", 100, 50..1000)
    private val includeSelf by boolValue("Include Self", true)
    private val includeOthers by boolValue("Include Others", true)
    private val spamCount by intValue("Spam Count", 2, 1..5)
    
    private var spamJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun onEnabled() {
        super.onEnabled()
        startSpamming()
        if (isSessionCreated) {
            session.displayClientMessage("§a§lEnabled §eXP Spam§a.")
        }
    }
    
    override fun onDisabled() {
        super.onDisabled()
        stopSpamming()
        if (isSessionCreated) {
            session.displayClientMessage("§c§lDisabled §eXP Spam§c.")
        }
    }
    
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (spamJob?.isActive != true) {
                startSpamming()
            }
        }
    }
    
    private fun startSpamming() {
        stopSpamming()
        spamJob = scope.launch {
            while (isEnabled) {
                try {
                    spamXPToTargets()
                    delay(intervalValue.toLong())
                } catch (e: Exception) {
                    Log.e("SpamXPElement", "Error during XP spam: ${e.message}")
                }
            }
        }
    }
    
    private fun stopSpamming() {
        spamJob?.cancel()
        spamJob = null
    }
    
    private fun spamXPToTargets() {
        val targets = mutableListOf<Long>()
        
        if (includeSelf) {
            targets.add(session.localPlayer.runtimeEntityId)
        }
        
        if (includeOthers) {
            session.level.entityMap.values
                .filterIsInstance<Player>()
                .filter { it !is LocalPlayer }
                .forEach { player ->
                    targets.add(player.runtimeEntityId)
                }
        }
        
        targets.forEach { runtimeEntityId ->
            repeat(spamCount) {
                sendRandomXP(runtimeEntityId)
            }
        }
    }
    
    private fun sendRandomXP(runtimeEntityId: Long) {
        try {
            val randomValue = if (Random.nextBoolean()) {
                -(Random.nextInt(1, 24792))
            } else {
                Random.nextInt(1, 24792)
            }
            
            val entityEventPacket = EntityEventPacket().apply {
                this.runtimeEntityId = runtimeEntityId
                type = EntityEventType.PLAYER_ADD_XP_LEVELS
                data = randomValue
            }
            
            session.serverBound(entityEventPacket)
        } catch (e: Exception) {
            Log.e("SpamXPElement", "Error sending XP packet: ${e.message}")
        }
    }
}