import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.util.AssetManager

class CmdListener(private val moduleManager: GameManager) : Element(
    name = "ChatListener",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_chat_listener")
) {
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {

    }
} 