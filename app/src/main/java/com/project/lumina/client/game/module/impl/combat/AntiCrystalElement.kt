package com.project.lumina.client.game.module.impl.combat

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import com.project.lumina.client.util.AssetManager

class AntiCrystalElement(iconResId: Int = AssetManager.getAsset("dice_d20_36")) : Element(
    name = "AntiCrystal",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_anti_crystal_display_name")
) {
    private var ylevel by floatValue("Y Level", 0.4f, 0.1f..1.61f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val original = interceptablePacket.packet
        if (original is PlayerAuthInputPacket) {

            val modified = PlayerAuthInputPacket().apply {

                rotation = original.rotation
                position = original.position.add(0.0, -ylevel.toDouble(), 0.0)
                motion = original.motion
                inputData.addAll(original.inputData)
                inputMode = original.inputMode
                playMode = original.playMode
                vrGazeDirection = original.vrGazeDirection
                tick = original.tick
                delta = original.delta
                itemUseTransaction = original.itemUseTransaction
                itemStackRequest = original.itemStackRequest
                playerActions.addAll(original.playerActions)
                inputInteractionModel = original.inputInteractionModel
                interactRotation = original.interactRotation
                analogMoveVector = original.analogMoveVector
                predictedVehicle = original.predictedVehicle
                vehicleRotation = original.vehicleRotation
                cameraOrientation = original.cameraOrientation
                rawMoveVector = original.rawMoveVector
            }

            interceptablePacket.intercept()
            session.serverBound(modified)
        }
    }
}