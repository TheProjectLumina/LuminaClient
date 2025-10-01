package com.project.lumina.client.constructors


import android.content.Context
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.game.module.impl.combat.AntiCrystalElement
import com.project.lumina.client.game.module.impl.combat.CritBotElement
import com.project.lumina.client.game.module.impl.combat.InfiniteAuraElement
import com.project.lumina.client.game.module.impl.combat.KillauraElement
import com.project.lumina.client.game.module.impl.combat.QuickAttackElement
import com.project.lumina.client.game.module.impl.world.StrafeElement
import com.project.lumina.client.game.module.impl.combat.TPAuraElement
import com.project.lumina.client.game.module.impl.combat.TriggerBotElement
import com.project.lumina.client.game.module.impl.combat.VelocityElement
import com.project.lumina.client.game.module.api.config.ConfigManagerElement
import com.project.lumina.client.game.module.impl.world.HasteElement
import com.project.lumina.client.game.module.impl.misc.AntiKickElement
import com.project.lumina.client.game.module.impl.misc.ArrayListElement
import com.project.lumina.client.game.module.impl.misc.CrasherElement
import com.project.lumina.client.game.module.impl.misc.DesyncElement
import com.project.lumina.client.game.module.impl.combat.AutoTotemElement

import com.project.lumina.client.game.module.impl.world.NoClipElement
import com.project.lumina.client.game.module.impl.misc.PositionLoggerElement
import com.project.lumina.client.game.module.impl.world.AutoXPElement
import com.project.lumina.client.game.module.impl.misc.SpamXPElement
import com.project.lumina.client.game.module.impl.misc.CoordSnatcherElement
import com.project.lumina.client.game.module.impl.misc.BlinderElement
import com.project.lumina.client.game.module.impl.misc.DeathTrackerElement
import com.project.lumina.client.game.module.impl.misc.SessionInfoElement
import com.project.lumina.client.game.module.impl.misc.SpeedoMeterElement
import com.project.lumina.client.game.module.impl.misc.WaterMarkElement
import com.project.lumina.client.game.module.impl.motion.AirJumpElement
import com.project.lumina.client.game.module.impl.motion.AntiAFKElement
import com.project.lumina.client.game.module.impl.world.AutoWalkElement
import com.project.lumina.client.game.module.impl.world.ChestStealerElement
import com.project.lumina.client.game.module.impl.motion.BhopElement
import com.project.lumina.client.game.module.impl.combat.DamageBoostElement
import com.project.lumina.client.game.module.impl.combat.HitboxElement
import com.project.lumina.client.game.module.impl.motion.FlyElement
import com.project.lumina.client.game.module.impl.world.FollowBotElement
import com.project.lumina.client.game.module.impl.motion.FullStopElement
import com.project.lumina.client.game.module.impl.motion.GlideElement
import com.project.lumina.client.game.module.impl.motion.HighJumpElement
import com.project.lumina.client.game.module.impl.motion.JetPackElement
import com.project.lumina.client.game.module.impl.motion.JitterFlyElement
import com.project.lumina.client.game.module.impl.motion.LongJumpElement
import com.project.lumina.client.game.module.impl.motion.MotionFlyElement
import com.project.lumina.client.game.module.impl.combat.OpFightBotElement
import com.project.lumina.client.game.module.api.commands.CmdListener
import com.project.lumina.client.game.module.impl.combat.LockHeedElement
import com.project.lumina.client.game.module.impl.misc.KeyStrokes
import com.project.lumina.client.game.module.impl.misc.TargetHud
import com.project.lumina.client.game.module.impl.visual.NoFireElement
import com.project.lumina.client.game.module.impl.misc.ToggleSound
import com.project.lumina.client.game.module.impl.motion.AntiACFly
import com.project.lumina.client.game.module.impl.world.PhaseElement
import com.project.lumina.client.game.module.impl.motion.SpeedElement
import com.project.lumina.client.game.module.impl.motion.SpiderElement
import com.project.lumina.client.game.module.impl.motion.StepElement
import com.project.lumina.client.game.module.impl.visual.AntiBlindElement
import com.project.lumina.client.game.module.impl.visual.ESPElement
import com.project.lumina.client.game.module.impl.visual.FreeCameraElement
import com.project.lumina.client.game.module.impl.visual.FullBrightElement
import com.project.lumina.client.game.module.impl.visual.NameTagElement
import com.project.lumina.client.game.module.impl.visual.NoHurtCameraElement
import com.project.lumina.client.game.module.impl.visual.ZoomElement
import com.project.lumina.client.game.module.impl.world.AutoNavigatorElement
import com.project.lumina.client.game.module.impl.world.MinimapElement
import com.project.lumina.client.game.module.impl.misc.PingSpoofElement
import com.project.lumina.client.game.module.impl.motion.FarSightElement
import com.project.lumina.client.game.module.impl.visual.DamageTextElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import com.project.lumina.client.service.Services
import java.io.File

object GameManager {

    private val _elements: MutableList<Element> = ArrayList()

    val elements: List<Element> = _elements

    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        with(_elements) {
            add(FlyElement())
            add(ZoomElement())
            add(AirJumpElement())
            add(AutoWalkElement())
            add(NoClipElement())
            add(HasteElement())
            add(SpeedElement())
            add(JetPackElement())
            add(HighJumpElement())
            add(BhopElement())
            add(NoHurtCameraElement())
            add(AntiAFKElement())
            add(PositionLoggerElement())


            add(CoordSnatcherElement())
            add(BlinderElement())
            add(DeathTrackerElement())
            add(MotionFlyElement())
            add(FreeCameraElement())
            add(KillauraElement())
            add(GlideElement())
            add(StepElement())
            add(LongJumpElement())
            add(SpiderElement())
            add(TPAuraElement())
            add(StrafeElement())
            add(FullStopElement())
            add(JitterFlyElement())
            add(PhaseElement())
            add(TriggerBotElement())
            add(CritBotElement())
            add(InfiniteAuraElement())
            add(DamageBoostElement())
            add(FullBrightElement())
            add(OpFightBotElement())
            add(FollowBotElement())
            add(VelocityElement())
            add(AntiKickElement())
            add(QuickAttackElement())
            add(AutoNavigatorElement())
            add(ConfigManagerElement())
            add(AntiCrystalElement())
            add(ChestStealerElement())
            add(AutoTotemElement())
            add(CrasherElement())


            add(NoFireElement())
            add(AntiACFly())
            add(LockHeedElement())
            add(FarSightElement())
            add(DamageTextElement())
            add(PingSpoofElement())
            add(CmdListener(this@GameManager))

            if (Services.RemisOnline == false){
                add(SpeedoMeterElement())
                add(SessionInfoElement())
                add(ArrayListElement())
                add(WaterMarkElement())
                add(MinimapElement())
                add(ToggleSound())
                add(DesyncElement())
                add(HitboxElement())
                add(KeyStrokes())
                add(TargetHud())
                add(NameTagElement())
                add(AntiBlindElement())
                add(ESPElement())

            }
        }


     
    }


    var netBound: NetBound? = null
        private set

    fun setNetBound(netBound: NetBound) {
        this.netBound = netBound
    }

    fun clearNetBound() {
        this.netBound = null
    }

    fun getCurrentPlayers(): List<GameDataManager.PlayerInfo> {
        return netBound?.getCurrentPlayers() ?: emptyList()
    }

    fun getPlayerCount(): Int {
        return netBound?.getPlayerCount() ?: 0
    }

    fun isConnected(): Boolean {
        return netBound != null
    }


    fun getModule(name: String): Element? {
        return elements.find { it.name.equals(name, ignoreCase = true) }
    }

    fun saveConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        saveConfigToFile(config)
    }

    fun saveConfigToFile(configFile: File) {
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _elements.forEach {
                    put(it.name, it.toJson())
                }
            })
        }

        configFile.writeText(json.encodeToString(jsonObject))
    }

    fun loadConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        if (!config.exists()) return


        loadConfigFromFile(config)
    }

    fun loadConfigFromFile(configFile: File) {
        if (!configFile.exists()) {
            return
        }

        val jsonString = configFile.readText()
        if (jsonString.isEmpty()) {
            return
        }

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val modules = jsonObject["modules"]!!.jsonObject
        _elements.forEach { module ->
            (modules[module.name] as? JsonObject)?.let {
                module.fromJson(it)
            }
        }
    }
}