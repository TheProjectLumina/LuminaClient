package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket

class ModalDialogElement : Element(
    name = "time_shift",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_time_shift_display_name")
) {


    override fun onEnabled() {
        super.onEnabled()

        val formJson = mapOf(
            "type" to "form",
            "title" to "Choose an Option",
            "content" to "Pick one of the buttons below.",
            "buttons" to listOf(
                mapOf("text" to "Option 1", "image" to mapOf("type" to "path", "data" to "textures/blocks/dirt")),
                mapOf("text" to "Option 2")
            )
        )

        val formString = com.google.gson.Gson().toJson(formJson)

        session.clientBound(ModalFormRequestPacket().apply {
            formId = 69
            formData = formString
        })



    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {

            return
        }





    }

}