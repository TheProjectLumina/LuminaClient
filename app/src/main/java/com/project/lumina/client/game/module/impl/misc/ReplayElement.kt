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

package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.* 
import org.cloudburstmc.math.vector.Vector3f 
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.project.lumina.client.application.AppContext 
import java.io.File
import kotlin.concurrent.thread

class ReplayElement : Element(
    name = "Replay",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_replay_display_name")
) {

    private val recordingInterval by intValue("Interval", 50, 20..200)
    private val autoSave by boolValue("Auto Save", true)
    private val playbackSpeed by floatValue("Speed", 1.0f, 0.1f..3.0f)
    private val recordInputs by boolValue("Record Inputs", true)

    @Serializable
    private data class ReplayFrame(
        val position: Vector3fData,
        val rotation: Vector3fData,
        val inputs: Set<String>,
        val timestamp: Long
    )

    @Serializable
    private data class Vector3fData(
        val x: Float,
        val y: Float,
        val z: Float
    )

    private var isRecording = false
    private var isPlaying = false
    private val frames = mutableListOf<ReplayFrame>()
    private var recordingStartTime = 0L
    private var lastRecordTime = 0L
    private var playbackThread: Thread? = null
    private var originalPosition: Vector3f? = null

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (isRecording) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastRecordTime >= recordingInterval) {
                    frames.add(
                        ReplayFrame(
                        Vector3fData(
                            packet.position.x,
                            packet.position.y,
                            packet.position.z
                        ),
                        Vector3fData(
                            packet.rotation.x,
                            packet.rotation.y,
                            packet.rotation.z
                        ),
                        packet.inputData.map { it.name }.toSet(),
                        currentTime - recordingStartTime
                    )
                    )
                    lastRecordTime = currentTime
                }
            }

            if (isPlaying) {
                interceptablePacket.intercept()
            }
        }
    }

    override fun onEnabled() {
        super.onEnabled()
        try {
            session.displayClientMessage("""
                §l§b[Replay] §r§7Commands:
                §f.replay record §7- Start recording
                §f.replay play §7- Play last recording
                §f.replay stop §7- Stop recording/playback
                §f.replay save <name> §7- Save recording
                §f.replay load <name> §7- Load recording
            """.trimIndent())
        } catch (e: Exception) {
            println("Error displaying Replay commands: ${e.message}")
        }
    }

    fun startRecording() {
        if (isPlaying) {
            session.displayClientMessage("§cCannot start recording while playing")
            return
        }

        frames.clear()
        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        lastRecordTime = recordingStartTime

        
        originalPosition = session.localPlayer.vec3Position

        session.displayClientMessage("§aStarted recording movement")
    }

    fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        if (autoSave) {
            saveReplay("replay_${System.currentTimeMillis()}")
        }
        session.displayClientMessage("§cStopped recording (${frames.size} frames)")
    }

    fun startPlayback() {
        if (isRecording) {
            session.displayClientMessage("§cCannot start playback while recording")
            return
        }

        if (frames.isEmpty()) {
            session.displayClientMessage("§cNo frames recorded")
            return
        }

        isPlaying = true

        
        originalPosition = session.localPlayer.vec3Position

        playbackThread = thread(name = "ReplayPlayback") {
            try {
                frames.forEachIndexed { index, frame ->
                    if (!isPlaying) return@thread

                    val delay = if (index < frames.size - 1) {
                        ((frames[index + 1].timestamp - frame.timestamp) / playbackSpeed).toLong()
                    } else 0L

                    
                    session.clientBound(SetEntityMotionPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        motion = Vector3f.from(
                            frame.position.x,
                            frame.position.y,
                            frame.position.z
                        )
                    })

                    
                    session.clientBound(MovePlayerPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        position = Vector3f.from(
                            frame.position.x,
                            frame.position.y,
                            frame.position.z
                        )
                        rotation = Vector3f.from(
                            frame.rotation.x,
                            frame.rotation.y,
                            frame.rotation.z
                        )
                        mode = MovePlayerPacket.Mode.NORMAL
                    })

                    
                    if (recordInputs) {
                        session.clientBound(PlayerAuthInputPacket().apply {
                            position = Vector3f.from(
                                frame.position.x,
                                frame.position.y,
                                frame.position.z
                            )
                            rotation = Vector3f.from(
                                frame.rotation.x,
                                frame.rotation.y,
                                frame.rotation.z
                            )
                            
                            val inputs = frame.inputs.mapNotNull { inputName ->
                                try {
                                    PlayerAuthInputData.valueOf(inputName)
                                } catch (e: IllegalArgumentException) {
                                    null
                                }
                            }
                            
                            inputData.addAll(inputs)
                        })
                    }

                    Thread.sleep(delay)
                }
            } catch (e: InterruptedException) {
                
            } finally {
                isPlaying = false
                
                originalPosition?.let { pos ->
                    session.clientBound(MovePlayerPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        position = pos
                        rotation = session.localPlayer.vec3Rotation
                        mode = MovePlayerPacket.Mode.NORMAL
                    })
                }
                session.displayClientMessage("§eReplay finished")
            }
        }
    }

    fun stopPlayback() {
        if (!isPlaying) return

        isPlaying = false
        playbackThread?.interrupt()
        playbackThread = null

        
        originalPosition?.let { pos ->
            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                position = pos
                rotation = session.localPlayer.vec3Rotation
                mode = MovePlayerPacket.Mode.NORMAL
            })
        }
        session.displayClientMessage("§cPlayback stopped")
    }

    fun saveReplay(name: String) {
        if (frames.isEmpty()) {
            session.displayClientMessage("§cNo frames to save")
            return
        }

        try {
            val replayDir = File(AppContext.instance.filesDir, "replays").apply {
                mkdirs()
            }
            val file = File(replayDir, "$name.json")
            file.writeText(Json.encodeToString(frames))
            session.displayClientMessage("§aSaved replay to $name")
        } catch (e: Exception) {
            session.displayClientMessage("§cFailed to save replay: ${e.message}")
        }
    }

    fun loadReplay(name: String) {
        try {
            val file = File(File(AppContext.instance.filesDir, "replays"), "$name.json")
            if (!file.exists()) {
                session.displayClientMessage("§cReplay file not found")
                return
            }

            frames.clear()
            frames.addAll(Json.decodeFromString(file.readText()))
            session.displayClientMessage("§aLoaded replay with ${frames.size} frames")
        } catch (e: Exception) {
            session.displayClientMessage("§cFailed to load replay: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        stopRecording()
        stopPlayback()
    }
}