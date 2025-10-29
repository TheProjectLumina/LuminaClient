package com.project.lumina.client.game.module.impl.combat

import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.inventory.PlayerInventory
import com.project.lumina.client.game.module.impl.world.ChestStealerElement
import com.project.lumina.client.game.utils.constants.Enchantment
import com.project.lumina.client.game.utils.misc.getEnchant
import com.project.lumina.client.util.AssetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AutoArmorElement(iconResId: Int = AssetManager.getAsset("ic_shield")) : Element(
    name = "AutoArmor",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_autoarmor_display_name")
) {

    private val delayMs by intValue("Delay", 50, 1..1000)
    private val preferProtection by boolValue("Prefer Protection", true)
    private val dropReplacedArmor by boolValue("Drop replaced armor", false)
    
    private var lastEquipTime = 0L
    private var isEquipping = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val armorMaterialValues = mapOf(
        "leather" to 1,
        "chainmail" to 2,
        "iron" to 3,
        "gold" to 2,
        "diamond" to 4,
        "netherite" to 5
    )

    private val armorSlotProtection = mapOf(
        "helmet" to mapOf(
            "leather" to 1, "chainmail" to 2, "iron" to 2, 
            "gold" to 2, "diamond" to 3, "netherite" to 3
        ),
        "chestplate" to mapOf(
            "leather" to 3, "chainmail" to 5, "iron" to 6, 
            "gold" to 5, "diamond" to 8, "netherite" to 8
        ),
        "leggings" to mapOf(
            "leather" to 2, "chainmail" to 4, "iron" to 5, 
            "gold" to 3, "diamond" to 6, "netherite" to 6
        ),
        "boots" to mapOf(
            "leather" to 1, "chainmail" to 1, "iron" to 2, 
            "gold" to 1, "diamond" to 3, "netherite" to 3
        )
    )

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        if (ChestStealerElement.isActivelyStealingFromChest) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEquipTime < delayMs || isEquipping) return

        if (!isSessionCreated) return

        coroutineScope.launch {
            try {
                isEquipping = true
                checkAndEquipBestArmor()
                lastEquipTime = System.currentTimeMillis()
                delay(delayMs.toLong())
            } finally {
                isEquipping = false
            }
        }
    }

    private suspend fun checkAndEquipBestArmor() {
        val inventory = session.localPlayer.inventory

        val armorSlots = listOf(
            PlayerInventory.SLOT_HELMET to "helmet",
            PlayerInventory.SLOT_CHESTPLATE to "chestplate",
            PlayerInventory.SLOT_LEGGINGS to "leggings",
            PlayerInventory.SLOT_BOOTS to "boots"
        )

        for ((armorSlot, armorType) in armorSlots) {
            val currentArmor = inventory.content[armorSlot]
            val bestArmorSlot = findBestArmorInInventory(armorType, inventory)

            if (bestArmorSlot != null) {
                val bestArmor = inventory.content[bestArmorSlot]
                
                if (shouldReplaceArmor(currentArmor, bestArmor, armorType)) {
                    try {
                        inventory.moveItem(bestArmorSlot, armorSlot, inventory, session)
                        delay(delayMs.toLong())
                    } catch (e: Exception) {
                        delay(delayMs.toLong())
                    }
                }
            }
        }

        if (dropReplacedArmor) {
            dropInferiorArmor()
        }
    }

    private fun findBestArmorInInventory(armorType: String, inventory: PlayerInventory): Int? {
        var bestSlot: Int? = null
        var bestScore = -1f

        for (i in 0 until 36) {
            val item = inventory.content[i]
            if (item == ItemData.AIR) continue

            if (isArmorOfType(item, armorType)) {
                val score = calculateArmorScore(item, armorType)
                if (score > bestScore) {
                    bestScore = score
                    bestSlot = i
                }
            }
        }

        return bestSlot
    }

    private fun shouldReplaceArmor(currentArmor: ItemData, newArmor: ItemData, armorType: String): Boolean {
        if (currentArmor == ItemData.AIR) return true

        val currentScore = calculateArmorScore(currentArmor, armorType)
        val newScore = calculateArmorScore(newArmor, armorType)

        return newScore > currentScore
    }

    private fun calculateArmorScore(item: ItemData, armorType: String): Float {
        if (item == ItemData.AIR) return 0f

        val identifier = item.definition.identifier
        var score = 0f

        val material = getMaterialFromIdentifier(identifier)
        if (material != null) {
            val baseProtection = armorSlotProtection[armorType]?.get(material) ?: 0
            score += baseProtection * 10f
        }

        if (preferProtection) {
            val protectionLevel = getEnchantmentLevel(item, Enchantment.PROTECTION_ALL)
            score += protectionLevel * 5f

            val fireProtectionLevel = getEnchantmentLevel(item, Enchantment.PROTECTION_FIRE)
            score += fireProtectionLevel * 3f

            val blastProtectionLevel = getEnchantmentLevel(item, Enchantment.PROTECTION_EXPLOSION)
            score += blastProtectionLevel * 3f

            val projectileProtectionLevel = getEnchantmentLevel(item, Enchantment.PROTECTION_PROJECTILE)
            score += projectileProtectionLevel * 3f
        }

        val unbreakingLevel = getEnchantmentLevel(item, Enchantment.DURABILITY)
        score += unbreakingLevel * 2f

        val thornsLevel = getEnchantmentLevel(item, Enchantment.THORNS)
        score += thornsLevel * 1f

        val durabilityPercent = if (item.damage > 0) {
            val maxDurability = getMaxDurability(identifier)
            if (maxDurability > 0) {
                ((maxDurability - item.damage).toFloat() / maxDurability) * 5f
            } else 5f
        } else 5f
        score += durabilityPercent

        return score
    }

    private fun isArmorOfType(item: ItemData, armorType: String): Boolean {
        val identifier = item.definition.identifier.lowercase()
        
        return when (armorType) {
            "helmet" -> identifier.contains("helmet")
            "chestplate" -> identifier.contains("chestplate")
            "leggings" -> identifier.contains("leggings")
            "boots" -> identifier.contains("boots")
            else -> false
        }
    }

    private fun getMaterialFromIdentifier(identifier: String): String? {
        return when {
            identifier.contains("leather") -> "leather"
            identifier.contains("chainmail") -> "chainmail"
            identifier.contains("iron") -> "iron"
            identifier.contains("gold") -> "gold"
            identifier.contains("diamond") -> "diamond"
            identifier.contains("netherite") -> "netherite"
            else -> null
        }
    }

    private fun getEnchantmentLevel(item: ItemData, enchantmentId: Short): Int {
        return item.getEnchant(enchantmentId)?.toInt() ?: 0
    }

    private fun getMaxDurability(identifier: String): Int {
        val material = getMaterialFromIdentifier(identifier) ?: return 0
        val armorType = when {
            identifier.contains("helmet") -> "helmet"
            identifier.contains("chestplate") -> "chestplate"
            identifier.contains("leggings") -> "leggings"
            identifier.contains("boots") -> "boots"
            else -> return 0
        }

        val durabilityMap = mapOf(
            "leather" to mapOf("helmet" to 55, "chestplate" to 80, "leggings" to 75, "boots" to 65),
            "chainmail" to mapOf("helmet" to 165, "chestplate" to 240, "leggings" to 225, "boots" to 195),
            "iron" to mapOf("helmet" to 165, "chestplate" to 240, "leggings" to 225, "boots" to 195),
            "gold" to mapOf("helmet" to 77, "chestplate" to 112, "leggings" to 105, "boots" to 91),
            "diamond" to mapOf("helmet" to 363, "chestplate" to 528, "leggings" to 495, "boots" to 429),
            "netherite" to mapOf("helmet" to 407, "chestplate" to 592, "leggings" to 555, "boots" to 481)
        )

        return durabilityMap[material]?.get(armorType) ?: 0
    }

    private suspend fun dropInferiorArmor() {
        val inventory = session.localPlayer.inventory

        val currentArmorScores = mapOf(
            "helmet" to calculateArmorScore(inventory.content[PlayerInventory.SLOT_HELMET], "helmet"),
            "chestplate" to calculateArmorScore(inventory.content[PlayerInventory.SLOT_CHESTPLATE], "chestplate"),
            "leggings" to calculateArmorScore(inventory.content[PlayerInventory.SLOT_LEGGINGS], "leggings"),
            "boots" to calculateArmorScore(inventory.content[PlayerInventory.SLOT_BOOTS], "boots")
        )

        for (i in 0 until 36) {
            val item = inventory.content[i]
            if (item == ItemData.AIR) continue

            val armorType = when {
                isArmorOfType(item, "helmet") -> "helmet"
                isArmorOfType(item, "chestplate") -> "chestplate"
                isArmorOfType(item, "leggings") -> "leggings"
                isArmorOfType(item, "boots") -> "boots"
                else -> null
            }

            if (armorType != null) {
                val itemScore = calculateArmorScore(item, armorType)
                val currentScore = currentArmorScores[armorType] ?: 0f

                if (itemScore <= currentScore) {
                    try {
                        inventory.dropItem(i, session)
                        delay(delayMs.toLong())
                    } catch (e: Exception) {
                        delay(delayMs.toLong())
                    }
                }
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        isEquipping = false
    }
}