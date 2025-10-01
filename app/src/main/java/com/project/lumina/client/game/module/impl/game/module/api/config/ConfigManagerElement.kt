import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.util.AssetManager

class ConfigManagerElement : Element(
    name = "config_manager",
    category = CheatCategory.Config,
    displayNameResId = AssetManager.getString("module_config_manager")
) {
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {

    }
} 