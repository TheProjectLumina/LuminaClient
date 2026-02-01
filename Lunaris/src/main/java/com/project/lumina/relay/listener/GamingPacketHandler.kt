package com.project.lumina.relay.listener

import com.project.lumina.relay.LuminaRelaySession
import com.project.lumina.relay.definition.CameraPresetDefinition
import com.project.lumina.relay.definition.Definitions
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.CameraPresetsPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.common.NamedDefinition
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry

@Suppress("MemberVisibilityCanBePrivate")
class GamingPacketHandler(
    val luminaRelaySession: LuminaRelaySession
) : LuminaRelayPacketListener {

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is StartGamePacket) {
            Definitions.itemDefinitions = SimpleDefinitionRegistry.builder<ItemDefinition>()
                .addAll(packet.itemDefinitions)
                .build()

            luminaRelaySession.client!!.peer.codecHelper.itemDefinitions = Definitions.itemDefinitions
            luminaRelaySession.server.peer.codecHelper.itemDefinitions = Definitions.itemDefinitions

            if (packet.isBlockNetworkIdsHashed) {
                luminaRelaySession.client!!.peer.codecHelper.blockDefinitions = Definitions.blockDefinitionsHashed
                luminaRelaySession.server.peer.codecHelper.blockDefinitions = Definitions.blockDefinitionsHashed
            } else {
                luminaRelaySession.client!!.peer.codecHelper.blockDefinitions = Definitions.blockDefinitions
                luminaRelaySession.server.peer.codecHelper.blockDefinitions = Definitions.blockDefinitions
            }
        }
        if (packet is CameraPresetsPacket) {
            val cameraDefinitions =
                SimpleDefinitionRegistry.builder<NamedDefinition>()
                    .addAll(List(packet.presets.size) {
                        CameraPresetDefinition.fromCameraPreset(packet.presets[it], it)
                    })
                    .build()

            luminaRelaySession.client!!.peer.codecHelper.cameraPresetDefinitions = cameraDefinitions
            luminaRelaySession.server.peer.codecHelper.cameraPresetDefinitions = cameraDefinitions
        }
        return false
    }

}