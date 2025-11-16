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
       // Log.i(TAG, "AutoTotem enabled - delay: ${delay}ms, lowHealth: $onlyWhenLowHealth, threshold: $healthThreshold, replaceOffhand: $replaceOffhand")
    }

    override fun onDisabled() {
        super.onDisabled()
     //   Log.i(TAG, "AutoTotem disabled")
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
          //  Log.v(TAG, "Health check: current=$currentHealth, threshold=$healthThreshold")
            if (currentHealth > healthThreshold) return
        }

        val offhandItem = session.localPlayer.inventory.offhand
        //Log.v(TAG, "Offhand item: ${offhandItem.definition?.identifier ?: "AIR"} x${offhandItem.count}")

        if (isTotem(offhandItem)) {
        //    Log.v(TAG, "Totem already in offhand, skipping")
            return
        }

        val totemSlot = findTotemInInventory()
        if (totemSlot == null) {
         //   Log.v(TAG, "No totem found in inventory")
            return
        }

        if (!replaceOffhand && offhandItem != ItemData.AIR) {
           // Log.v(TAG, "Offhand occupied and replace disabled: ${offhandItem.definition?.identifier}")
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
     //   Log.v(TAG, "Item check: $identifier -> isTotem: $isTotem")
        return isTotem
    }

    private fun findTotemInInventory(): Int? {
        val inventory = session.localPlayer.inventory
    //    Log.v(TAG, "Searching for totem in inventory slots 0-35")

        for (i in 0 until 36) {
            val item = inventory.content[i]
            if (isTotem(item)) {
        //        Log.d(TAG, "Found totem in slot $i: ${item.definition?.identifier} x${item.count}")
                return i
            }
        }

    //    Log.v(TAG, "No totem found in inventory")
        return null
    }

    private fun moveTotemToOffhand(sourceSlot: Int) {
        try {
            val inventory = session.localPlayer.inventory

            val sourceItem = inventory.content[sourceSlot]
            if (!isTotem(sourceItem)) {
       //         Log.w(TAG, "Totem no longer exists in slot $sourceSlot: ${sourceItem.definition?.identifier ?: "AIR"}")
                return
            }

            val offhandItem = inventory.offhand
//            Log.d(TAG, "Moving totem from slot $sourceSlot to offhand")
//            Log.d(TAG, "Source: ${sourceItem.definition?.identifier} x${sourceItem.count}")
//            Log.d(TAG, "Current offhand: ${offhandItem.definition?.identifier ?: "AIR"} x${offhandItem.count}")

            inventory.moveItem(sourceSlot, 40, inventory, session)

     //       Log.i(TAG, "Totem move operation completed successfully")
        } catch (e: Exception) {
            //Log.e(TAG, "Failed to move totem to offhand", e)
        }
    }
}