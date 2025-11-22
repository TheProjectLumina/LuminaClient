package com.project.lumina.client.game.module.impl.combat

import android.util.Log
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.util.AssetManager

import com.project.lumina.client.game.utils.constants.Attribute
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AutoTotemElement(iconResId: Int = AssetManager.getAsset("ic_shield")) : Element(
    name = "AutoTotem",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_autototem_display_name")
) {

    private val delay by intValue("Delay", 100, 0..1000)
    private val onlyWhenLowHealth by boolValue("Only When Low Health", false)
    private val healthThreshold by intValue("Health Threshold", 10, 1..20)
    private val replaceOffhand by boolValue("Replace Offhand", true)

    private var lastTotemTime = 0L

    companion object {
        private const val TAG = "AutoTotem"
    }

    override fun onEnabled() {
        super.onEnabled()
       
    }

    override fun onDisabled() {
        super.onDisabled()
     
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTotemTime < delay) return

        if (onlyWhenLowHealth) {
            val healthAttribute = session.localPlayer.attributes[Attribute.HEALTH]
            val currentHealth = healthAttribute?.value ?: 20f
          
            if (currentHealth > healthThreshold) return
        }

        val offhandItem = session.localPlayer.inventory.offhand
        //Log.v(TAG, "Offhand item: ${offhandItem.definition?.identifier ?: "AIR"} x${offhandItem.count}")

        if (isTotem(offhandItem)) {
        
            return
        }

        val totemSlot = findTotemInInventory()
        if (totemSlot == null) {
         
            return
        }

        if (!replaceOffhand && offhandItem != ItemData.AIR) {
           
            return
        }

        //Log.i(TAG, "Moving totem from slot $totemSlot to offhand")
        moveTotemToOffhand(totemSlot)
        lastTotemTime = currentTime
    }

    private fun isTotem(item: ItemData): Boolean {
        if (item == ItemData.AIR) return false
        val identifier = item.definition?.identifier
        val isTotem = identifier == "minecraft:totem_of_undying"
     
        return isTotem
    }

    private fun findTotemInInventory(): Int? {
        val inventory = session.localPlayer.inventory
    

        for (i in 0 until 36) {
            val item = inventory.content[i]
            if (isTotem(item)) {
        
                return i
            }
        }

    
        return null
    }

    private fun moveTotemToOffhand(sourceSlot: Int) {
        try {
            val inventory = session.localPlayer.inventory

            val sourceItem = inventory.content[sourceSlot]
            if (!isTotem(sourceItem)) {
       
                return
            }

            val offhandItem = inventory.offhand




            inventory.moveItem(sourceSlot, 40, inventory, session)

     
        } catch (e: Exception) {
            //Log.e(TAG, "Failed to move totem to offhand", e)
        }
    }
}