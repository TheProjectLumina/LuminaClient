package com.project.lumina.client.game.module.impl.visual

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import com.project.lumina.client.util.AssetManager

class ZoomElement(iconResId: Int = AssetManager.getAsset("ic_zoom_in_black_24dp")) : Element(
    name = "Zoom",
    category = CheatCategory.Visual,
    iconResId,
    displayNameResId = AssetManager.getString("module_zoom_display_name")
) {


    private val enableZoomPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS
                )
            )
            walkSpeed = 7.0f
        })
    }


    private val disableZoomPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS
                )
            )
            walkSpeed = 0.1f
        })
    }

    private var isZoomEnabled = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (!isZoomEnabled && isEnabled) {

                enableZoomPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                session.clientBound(enableZoomPacket)
                isZoomEnabled = true
            } else if (isZoomEnabled && !isEnabled) {

                disableZoomPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
                session.clientBound(disableZoomPacket)
                isZoomEnabled = false
            }
        }
    }
}
