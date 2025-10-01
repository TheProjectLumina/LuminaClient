/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * This is open source — not open credit.
 *
 * If you're here to build, welcome. If you're here to repaint and reupload
 * with your tag slapped on it… you're not fooling anyone.
 *
 * Changing colors and class names doesn't make you a developer.
 * Copy-pasting isn't contribution.
 *
 * You have legal permission to fork. But ask yourself — are you improving,
 * or are you just recycling someone else's work to feed your ego?
 *
 * Open source isn't about low-effort clones or chasing clout.
 * It's about making things better. Sharper. Cleaner. Smarter.
 *
 * So go ahead, fork it — but bring something new to the table,
 * or don't bother pretending.
 *
 * This message is philosophical. It does not override your legal rights under GPLv3.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * GPLv3 Summary:
 * - You have the freedom to run, study, share, and modify this software.
 * - If you distribute modified versions, you must also share the source code.
 * - You must keep this license and copyright intact.
 * - You cannot apply further restrictions — the freedom stays with everyone.
 * - This license is irrevocable, and applies to all future redistributions.
 *
 * Full text: https://www.gnu.org/licenses/gpl-3.0.html
 */


package com.project.lumina.client.game.module.impl.combat

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import com.project.lumina.client.util.AssetManager
import com.project.lumina.client.game.module.api.setting.stringValue

class VelocityElement(iconResId: Int = AssetManager.getAsset("ic_file_download_black_24dp")) : Element(
    name = "Velocity",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_velocity_display_name")
) {
    private val mode by stringValue(this, "Mode", "Percentage", listOf("Vanilla", "Percentage", "Blocks"))

    private val horizontalPercent by floatValue("Horizontal %", 80f, 0f..100f)
    private val verticalPercent by floatValue("Vertical %", 80f, 0f..100f)

    private val horizontalBlocks by floatValue("Horizontal Blocks", 0.2f, 0f..2f)
    private val verticalBlocks by floatValue("Vertical Blocks", 0.2f, 0f..2f)

    private object Vanilla {
        fun handlePacket(interceptablePacket: InterceptablePacket) {
            if (interceptablePacket.packet is SetEntityMotionPacket) {
                interceptablePacket.isIntercepted = true
            }
        }
    }

    private object Percentage {
        fun handlePacket(interceptablePacket: InterceptablePacket, horizontalPercent: Float, verticalPercent: Float) {
            if (interceptablePacket.packet is SetEntityMotionPacket) {
                val packet = interceptablePacket.packet as SetEntityMotionPacket
                val horizontalMultiplier = horizontalPercent / 100f
                val verticalMultiplier = verticalPercent / 100f
                packet.motion = packet.motion.mul(horizontalMultiplier, verticalMultiplier, horizontalMultiplier)
            }
        }
    }

    private object Blocks {
        fun handlePacket(interceptablePacket: InterceptablePacket, horizontalBlocks: Float, verticalBlocks: Float) {
            if (interceptablePacket.packet is SetEntityMotionPacket) {
                val packet = interceptablePacket.packet as SetEntityMotionPacket
                val originalMotion = packet.motion

                val newHorizontalX = if (originalMotion.x > 0) horizontalBlocks else -horizontalBlocks
                val newHorizontalZ = if (originalMotion.z > 0) horizontalBlocks else -horizontalBlocks
                val newVertical = if (originalMotion.y > 0) verticalBlocks else -verticalBlocks

                packet.motion = org.cloudburstmc.math.vector.Vector3f.from(
                    newHorizontalX.toDouble(),
                    newVertical.toDouble(),
                    newHorizontalZ.toDouble()
                )
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }
        
        if (interceptablePacket.packet is SetEntityMotionPacket) {
            val packet = interceptablePacket.packet as SetEntityMotionPacket
            if (packet.runtimeEntityId == session.localPlayer.runtimeEntityId) {
                when (mode) {
                    "Vanilla" -> Vanilla.handlePacket(interceptablePacket)
                    "Percentage" -> Percentage.handlePacket(interceptablePacket, horizontalPercent, verticalPercent)
                    "Blocks" -> Blocks.handlePacket(interceptablePacket, horizontalBlocks, verticalBlocks)
                }
            }
        }
    }
}