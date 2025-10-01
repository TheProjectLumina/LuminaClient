package com.project.lumina.client.game.module.impl.motion

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin


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
class LongJumpElement(iconResId: Int = AssetManager.getAsset("ic_waves_black_24dp")) : Element(
    name = "LongJump",
    category = CheatCategory.Motion,
    iconResId,
    displayNameResId = AssetManager.getString("module_long_jump_display_name")
) {

    private var jumpHeight by floatValue("Jump Height", 1f, 0.5f..5f)
    private var forwardBoost by floatValue("Boost", 1.2f, 0.5f..3.0f)

    private var jumped = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val input = packet.inputData

            val isJumping = input.contains(PlayerAuthInputData.JUMP_DOWN)
            val isOnGround = input.contains(PlayerAuthInputData.VERTICAL_COLLISION)

            if (isJumping && isOnGround && !jumped) {
                val yaw = Math.toRadians(packet.rotation.y.toDouble())
                val motionX = (-sin(yaw) * forwardBoost).toFloat()
                val motionZ = (cos(yaw) * forwardBoost).toFloat()

                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(motionX, jumpHeight, motionZ)
                }

                session.clientBound(motionPacket)
                jumped = true
            }

            if (isOnGround && !isJumping) {
                jumped = false
            }
        }
    }
}
