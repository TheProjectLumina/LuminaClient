package com.project.lumina.client.game.module.api.commands

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.project.lumina.client.R
import com.project.lumina.client.constructors.BoolValue
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.FloatValue
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.constructors.IntValue
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.remlink.TerminalViewModel
import com.project.lumina.client.util.TextComponentUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import java.net.URL

@Serializable
data class RoleConfig(
    val roles: Map<String, List<String>>,
    val roleFormats: Map<String, String>
)

class CmdListener(private val moduleManager: GameManager) : Element(
    name = "ChatListener",
    category = CheatCategory.Misc,
    displayNameResId = R.string.module_chat_listener
) {

    private var roleConfig: RoleConfig? = null
    private val json = Json { ignoreUnknownKeys = true }
    private val CONFIG_URL = "https://nyxelle.netlify.app/Files/service/tags.json"
    private val gameInfoCommands = GameInfoCommands(this, moduleManager)

    init {
        loadConfig()
    }

    companion object {
        const val PREFIX = "!"
        const val FEEDBACK_ENABLED = true
        const val HEADER_COLOR = "§6"
        const val ACCENT_COLOR = "§e"
        const val SUCCESS_COLOR = "§a"
        const val ERROR_COLOR = "§c"
        const val INFO_COLOR = "§7"
        const val VALUE_COLOR = "§b"

        var isModuleEnabled by mutableStateOf(true)
    }

    private var isInGame by mutableStateOf(false)

    fun interceptOutboundPacket(interceptablePacket: InterceptablePacket) {
        if (!isModuleEnabled) {
         //   Log.d("CmdListener", "Module disabled, skipping packet")
            return
        }

        if (interceptablePacket.packet is TextPacket) {
            val packet = interceptablePacket.packet as TextPacket
            val message = packet.message.toString().trim()
            Log.d("CmdListener", "Outbound TextPacket: $message")

            if (message.startsWith(PREFIX)) {
                interceptablePacket.isIntercepted = true
                processCommand(message)
                TerminalViewModel.addTerminalLog("GameSession", "Command intercepted and not sent to server: $message")
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isModuleEnabled) {
           // Log.d("CmdListener", "Module disabled, skipping packet")
            return
        }

        val packet = interceptablePacket.packet

        if (packet is TextPacket) {
            try {
                val originalMessage = packet.message.toString()
                Log.d("CmdListener", "Processing TextPacket: $originalMessage")
                val extractedUsername = extractUsername(originalMessage)
                if (extractedUsername != null) {
                    Log.d("CmdListener", "Extracted username: $extractedUsername")
                    val role = getRoleForUser(extractedUsername)
                    if (role != null) {
                        val rolePrefix = getRolePrefix(role)
                        Log.d("CmdListener", "Applying role prefix: $rolePrefix for role: $role")
                        val newPacket = TextPacket().apply {
                            type = packet.type
                            sourceName = packet.sourceName
                            xuid = packet.xuid
                            platformChatId = packet.platformChatId
                            message =
                                originalMessage.replace(
                                    extractedUsername,
                                    "$rolePrefix $extractedUsername",
                                    ignoreCase = false
                                )

                        }
                        interceptablePacket.intercept()
                        session.clientBound(newPacket)
                        Log.i("CmdListener", "Sent TextPacket with role prefix: ${newPacket.message}")
                        return
                    }
                }

                var modifiedMessage = originalMessage
                var wasModified = false

                roleConfig?.roles?.forEach { (role, users) ->
                    users.forEach { username ->
                        if (modifiedMessage.contains(username, ignoreCase = false)) {
                            val rolePrefix = getRolePrefix(role)
                            val newModifiedMessage = modifiedMessage.replace(username, "$rolePrefix $username")
                            if (newModifiedMessage != modifiedMessage) {
                                modifiedMessage = newModifiedMessage
                                wasModified = true
                                Log.d("CmdListener", "Modified message with role $role for user $username: $modifiedMessage")
                            }
                        }
                    }
                }
                if (wasModified) {
                    val newPacket = TextPacket().apply {
                        type = packet.type
                        sourceName = packet.sourceName
                        xuid = packet.xuid
                        platformChatId = packet.platformChatId
                        message = modifiedMessage
                    }
                    interceptablePacket.intercept()
                    session.clientBound(newPacket)
                    Log.i("CmdListener", "Sent modified TextPacket: ${newPacket.message}")
                    return
                }

                // Check for commands in inbound packets
                val message = packet.message.trim()
                if (message.startsWith(PREFIX) && session.isProxyPlayer(packet.sourceName)) {
                    interceptablePacket.isIntercepted = true
                    processCommand(message.toString())
                    TerminalViewModel.addTerminalLog("GameSession", "Command intercepted: $message")
                }
            } catch (e: Exception) {
            //    Log.e("CmdListener", "Error processing TextPacket: ${e.message}", e)
            }
        }

        if (packet is PlayerAuthInputPacket) {
            isInGame = true
           // Log.d("CmdListener", "PlayerAuthInputPacket received, isInGame = true")
        }
    }

    private fun processCommand(message: String) {
        if (!message.startsWith(PREFIX)) {
            Log.d("CmdListener", "Message does not start with prefix: $message")
            return
        }

        Log.d("CmdListener", "Processing command: $message")

        if (!isSessionCreated || !FEEDBACK_ENABLED) {
            Log.w("CmdListener", "Command processing skipped: isSessionCreated=$isSessionCreated, FEEDBACK_ENABLED=$FEEDBACK_ENABLED")
            return
        }

        val args = message.substring(PREFIX.length).split(" ").filter { it.isNotBlank() }
        if (args.isEmpty()) {
            sendClientMessage("${ERROR_COLOR}Empty command. Use $ACCENT_COLOR!help")
            Log.d("CmdListener", "Empty command received")
            return
        }

        when (val command = args[0].lowercase()) {
            "toggle" -> handleToggle(args.getOrNull(1))
            "ping" -> handlePing()
            "help" -> handleHelp(args.getOrNull(1))
            "set" -> handleSet(args.getOrNull(1), args.getOrNull(2), args.getOrNull(3))
            "list" -> handleList()
            "reset" -> handleReset(args.getOrNull(1))
            "info" -> handleInfo(args.getOrNull(1))
            "seed" -> gameInfoCommands.handleSeed()
            "playerlist" -> gameInfoCommands.handlePlayerList()
            "spawn" -> gameInfoCommands.handleSpawn()
            "gamerules" -> gameInfoCommands.handleGameRules()
            "ids" -> gameInfoCommands.handleIds()
            "worldinfo" -> gameInfoCommands.handleWorldInfo()
            "minimap" -> gameInfoCommands.handleMinimap(args.drop(1))
            "gamemode" -> gameInfoCommands.handleGameMode()
            "coords" -> gameInfoCommands.handleCoords()
            "time" -> gameInfoCommands.handleTime()
            "biome" -> gameInfoCommands.handleBiome()
            "players" -> gameInfoCommands.handlePlayers(args.drop(1))
            "stats" -> gameInfoCommands.handleStats()
            "version" -> gameInfoCommands.handleVersion()
            else -> {
                sendClientMessage("${ERROR_COLOR}Unknown command: $ACCENT_COLOR$command$INFO_COLOR - Try $ACCENT_COLOR!help")
                Log.d("CmdListener", "Unknown command: $command")
            }
        }
    }

    fun sendClientMessage(message: String) {
        session.displayClientMessage(message, TextPacket.Type.RAW)
        TerminalViewModel.addTerminalLog("GameSession", "Feedback sent: $message")
    }

    private fun formatModuleName(rawName: String): String {
        return rawName.replace("_", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun parseModuleName(input: String): String {
        return input.replace(Regex("[^a-zA-Z0-9]"), "").lowercase()
    }

    private fun handleModuleToggle(state: String?) {
        if (state == null) {
            sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!module <on/off>")
            return
        }
        when (state.lowercase()) {
            "on" -> {
                isModuleEnabled = true
                sendClientMessage("${SUCCESS_COLOR}ChatListener module enabled")
            }
            "off" -> {
                isModuleEnabled = false
                sendClientMessage("${SUCCESS_COLOR}ChatListener module disabled")
            }
            else -> sendClientMessage("${ERROR_COLOR}Invalid state. Use 'on' or 'off'")
        }
        TerminalViewModel.addTerminalLog("GameSession", "Module enabled: $isModuleEnabled")
    }

    private fun handleToggle(moduleName: String?) {
        if (moduleName == null) {
            sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!toggle <module>")
            return
        }
        val parsedName = parseModuleName(moduleName)
        val module = moduleManager.getModule(parsedName)
            ?: return sendClientMessage("${ERROR_COLOR}Module '$ACCENT_COLOR$moduleName$ERROR_COLOR' not found.")

        module.isEnabled = !module.isEnabled
        val state = if (module.isEnabled) "on" else "off"
        val stateColor = if (module.isEnabled) SUCCESS_COLOR else ERROR_COLOR
        sendClientMessage("$HEADER_COLOR${formatModuleName(module.name)} $INFO_COLOR${stateColor}$state$INFO_COLOR.")
        TerminalViewModel.addTerminalLog("GameSession", "Toggled module ${module.name} to $state")
    }

    private fun handlePing() {
        sendClientMessage("${SUCCESS_COLOR}Pong!")
        TerminalViewModel.addTerminalLog("GameSession", "Executed ping command")
    }

    private fun handleHelp(moduleName: String?) {
        if (moduleName == null) {
            displayGeneralHelp()
        } else {
            displayModuleHelp(moduleName)
        }
    }

    private fun displayGeneralHelp() {
        sendClientMessage("${HEADER_COLOR}Command Help ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}Available Commands:")
        listOf(
            "!module <on/off> - Enable/disable this module",
            "!toggle <module> - Toggle module on/off",
            "!set <module> <setting> <value> - Set module setting",
            "!help [module] - Show this help or module details",
            "!list - List all modules",
            "!reset <module> - Reset module settings",
            "!info <module> - Show module information",
            "!ping - Test command response",
            "!seed - Show world seed",
            "!playerlist - List online players",
            "!spawn - Show default spawn coordinates",
            "!gamerules - Show game rules",
            "!ids - Show runtime and unique entity IDs",
            "!worldinfo - Show world information",
            "!gamemode - Show player and level game modes",
            "!coords - Show current player coordinates",
            "!time - Show in-game time information",
            "!biome - Show spawn biome information",
            "!players [name/uuid/entityid] <value> - Show player details",
            "!stats - Show game data statistics",
            "!version - Show game and protocol version"
        ).forEach { sendClientMessage("$INFO_COLOR- $ACCENT_COLOR$it") }
        TerminalViewModel.addTerminalLog("GameSession", "Displayed general help")
    }

    private fun displayModuleHelp(moduleName: String) {
        val parsedName = parseModuleName(moduleName)
        val module = moduleManager.getModule(parsedName)
            ?: return sendClientMessage("${ERROR_COLOR}Module '$ACCENT_COLOR$moduleName$ERROR_COLOR' not found.")

        sendClientMessage("$HEADER_COLOR${formatModuleName(module.name)} Help ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}Status: ${if (module.isEnabled) "${SUCCESS_COLOR}Enabled" else "${ERROR_COLOR}Disabled"}")
        val settings = module.values.filter { it is BoolValue || it is FloatValue || it is IntValue }
        if (settings.isNotEmpty()) {
            sendClientMessage("${ACCENT_COLOR}Settings:")
            settings.forEach { value ->
                val currentValue = when (value) {
                    is BoolValue -> if (value.value) "${SUCCESS_COLOR}true" else "${ERROR_COLOR}false"
                    is FloatValue -> "$VALUE_COLOR${value.value}$INFO_COLOR (Range: ${value.range})"
                    is IntValue -> "$VALUE_COLOR${value.value}$INFO_COLOR (Range: ${value.range})"
                    else -> "N/A"
                }
                sendClientMessage("$INFO_COLOR- $ACCENT_COLOR${value.name}: $currentValue")
            }
        } else {
            sendClientMessage("${INFO_COLOR}No configurable settings.")
        }
        TerminalViewModel.addTerminalLog("GameSession", "Displayed help for module $moduleName")
    }

    private fun handleSet(moduleName: String?, settingName: String?, valueString: String?) {
        if (moduleName == null || settingName == null || valueString == null) {
            sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!set <module> <setting> <value>")
            return
        }

        val parsedName = parseModuleName(moduleName)
        val module = moduleManager.getModule(parsedName)
            ?: return sendClientMessage("${ERROR_COLOR}Module '$ACCENT_COLOR$moduleName$ERROR_COLOR' not found.")

        val value = module.values.find { it.name.equals(settingName, ignoreCase = true) }
            ?: return sendClientMessage("${ERROR_COLOR}Setting '$ACCENT_COLOR$settingName$ERROR_COLOR' not found.")

        try {
            when (value) {
                is BoolValue -> value.value = valueString.toBooleanStrict()
                is FloatValue -> {
                    val newValue = valueString.toFloat()
                    if (newValue in value.range) value.value = newValue
                    else throw IllegalArgumentException("Value out of range ${value.range}")
                }
                is IntValue -> {
                    val newValue = valueString.toInt()
                    if (newValue in value.range) value.value = newValue
                    else throw IllegalArgumentException("Value out of range ${value.range}")
                }
                else -> throw IllegalArgumentException("Unsupported setting type")
            }
            sendClientMessage("$HEADER_COLOR${formatModuleName(module.name)}.${settingName} ${INFO_COLOR}set to $VALUE_COLOR$valueString$INFO_COLOR.")
            TerminalViewModel.addTerminalLog("GameSession", "Set ${module.name}.${settingName} to $valueString")
        } catch (e: Exception) {
            sendClientMessage("${ERROR_COLOR}Invalid '$ACCENT_COLOR$valueString$ERROR_COLOR' for $ACCENT_COLOR$settingName$ERROR_COLOR: ${e.message}")
            Log.e("CmdListener", "Failed to set $settingName: ${e.message}")
            TerminalViewModel.addTerminalLog("GameSession", "Failed to set ${module.name}.${settingName}: ${e.message}")
        }
    }

    private fun handleList() {
        val modules = moduleManager.elements.filter { !it.private }.sortedBy { formatModuleName(it.name) }
        if (modules.isEmpty()) {
            sendClientMessage("${ERROR_COLOR}No modules available.")
            return
        }

        sendClientMessage("${HEADER_COLOR}Module List ${INFO_COLOR}▼")
        modules.forEach { module ->
            val status = if (module.isEnabled) "${SUCCESS_COLOR}ON" else "${ERROR_COLOR}OFF"
            sendClientMessage("$INFO_COLOR- $ACCENT_COLOR${formatModuleName(module.name)} $INFO_COLOR[$status]")
        }
        sendClientMessage("${INFO_COLOR}Total: ${modules.size} modules")
        TerminalViewModel.addTerminalLog("GameSession", "Displayed module list")
    }

    private fun handleReset(moduleName: String?) {
        if (moduleName == null) {
            sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!reset <module>")
            return
        }

        val parsedName = parseModuleName(moduleName)
        val module = moduleManager.getModule(parsedName)
            ?: return sendClientMessage("${ERROR_COLOR}Module '$ACCENT_COLOR$moduleName$ERROR_COLOR' not found.")

        module.values.forEach { value ->
            when (value) {
                is BoolValue -> value.value = false
                is FloatValue -> value.value = value.range.start
                is IntValue -> value.value = value.range.first
                else -> return
            }
        }
        sendClientMessage("$HEADER_COLOR${formatModuleName(module.name)} ${INFO_COLOR}settings reset to defaults.")
        TerminalViewModel.addTerminalLog("GameSession", "Reset settings for module $moduleName")
    }

    private fun handleInfo(moduleName: String?) {
        if (moduleName == null) {
            sendClientMessage("${ERROR_COLOR}Usage: $ACCENT_COLOR!info <module>")
            return
        }

        val parsedName = parseModuleName(moduleName)
        val module = moduleManager.getModule(parsedName)
            ?: return sendClientMessage("${ERROR_COLOR}Module '$ACCENT_COLOR$moduleName$ERROR_COLOR' not found.")

        sendClientMessage("$HEADER_COLOR${formatModuleName(module.name)} Info ${INFO_COLOR}▼")
        sendClientMessage("${ACCENT_COLOR}Status: ${if (module.isEnabled) "${SUCCESS_COLOR}Enabled" else "${ERROR_COLOR}Disabled"}")
        sendClientMessage("${ACCENT_COLOR}Category: $INFO_COLOR${module.category}")
        sendClientMessage("${ACCENT_COLOR}Private: $INFO_COLOR${if (module.private) "Yes" else "No"}")
        TerminalViewModel.addTerminalLog("GameSession", "Displayed info for module $moduleName")
    }

    private fun loadConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val configText = URL(CONFIG_URL).readText()
                roleConfig = json.decodeFromString<RoleConfig>(configText)
                Log.i("ChatLoggerElement", "Successfully loaded role config: ${roleConfig?.roles?.keys}")
                Log.i("ChatLoggerElement", "Role formats: ${roleConfig?.roleFormats?.keys}")
            } catch (e: Exception) {
                Log.e("ChatLoggerElement", "Failed to load role config", e)
            }
        }
    }

    private fun getRoleForUser(username: String): String? {
        roleConfig?.roles?.forEach { (role, users) ->
            if (username in users) {
                return role
            }
        }
        return null
    }

    private fun getRolePrefix(role: String): String {
        return roleConfig?.roleFormats?.get(role) ?: "§7[$role]§r"
    }

    private fun stripColorCodes(text: String): String {
        return text.replace("§[0-9a-fk-or]".toRegex(), "")
    }

    private fun extractUsername(message: String): String? {
        val cleanMessage = stripColorCodes(message).trim()

        return when {
            cleanMessage.startsWith("<") && cleanMessage.contains(">") -> {
                val endIndex = cleanMessage.indexOf(">")
                if (endIndex > 1) cleanMessage.substring(1, endIndex) else null
            }
            cleanMessage.contains(":") -> {
                val parts = cleanMessage.split(":", limit = 2)
                if (parts.size >= 2) {
                    val usernamePart = parts[0].trim()
                    if (usernamePart.contains("]")) {
                        val bracketIndex = usernamePart.lastIndexOf("]")
                        if (bracketIndex < usernamePart.length - 1) {
                            usernamePart.substring(bracketIndex + 1).trim()
                        } else null
                    } else {
                        usernamePart
                    }
                } else null
            }
            else -> {
                val words = cleanMessage.split(" ")
                if (words.isNotEmpty()) words[0] else null
            }
        }
    }
}