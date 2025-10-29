package com.project.lumina.client.game

object TranslationManager {

    private val map = HashMap<String, Map<String, String>>()

    init {
        map["en"] = en()
    }

    private fun en() = buildMap {
        put("fly", "Fly")
        put("no_clip", "No Clip")
        put("zoom", "Zoom")
        put("step", "Step")
        put("spider", "Spider")
        put("phase", "Phase")
        put("long_jump", "Long Jump")
        put("jesus", "Jesus")
        put("glide", "Glide")
        put("faststop", "Full Stop")
        put("tpaura", "TP Aura")
        put("strafe", "Strafe")
        put("jitterfly", "JitterFly")
        put("triggerbot", "TriggerBot")
        put("air_jump", "Air Jump")
        put("speed", "Speed")
        put("full_bright", "Full Bright")
        put("haste", "Haste")
        put("jetpack", "Jetpack")
        put("levitation", "Levitation")
        put("high_jump", "High Jump")
        put("slow_falling", "Slow Falling")
        put("anti_knockback", "Velocity")
        put("poseidon", "Poseidon")
        put("regeneration", "regen")
        put("bhop", "BHOP")
        put("sprint", "Sprint")
        put("no_hurt_camera", "No HurtCam")
        put("anti_afk", "Anti AFK")
        put("auto_walk", "Auto Walk")
        put("desync", "Desync")
        put("position_logger", "Entity Tracer")
        put("killaura", "Killaura")
        put("motion_fly", "Motion Fly")
        put("free_camera", "FreeCam")
        put("player_tracer", "Player Tracker")
        put("critic", "Criticals")
        put("nausea", "Nausea")
        put("health_boost", "Health Boost")
        put("jump_boost", "Jump Boost")
        put("resistance", "Resistance")
        put("fire_resist", "Fire Resistance")
        put("swiftness", "Swiftness")
        put("instant_health", "Instant Health")
        put("strength", "Strength")
        put("instant_damage", "Instant Damage")
        put("anti_crystal", "Anti Crystal")
        put("bad_omen", "Bad Omen")
        put("conduit_power", "Conduit Power")
        put("darkness", "Darkness")
        put("fatal_poison", "Fatal Poison")
        put("hunger", "Hunger")
        put("poison", "Poison")
        put("village_omen", "Village Hero")
        put("weakness", "Weakness")
        put("wither", "Wither")
        put("night_vision", "Night Vision")
        put("invisibility", "Invisibility")
        put("saturation", "Saturation")
        put("absorption", "Absorption")
        put("blindness", "Blindness")
        put("hunger", "Hunger")
        put("time_shift", "Time Changer")
        put("weather_controller", "Weather Controller")
        put("crash", "Crasher")
        put("arraylist", "Array List")
        put("DamageBoost", "Damage Boost")
        put("infiniteaura", "Infinite Aura")
        put("opfightbot", "OpFight Bot")
        put("QuickAttack", "Quick Attack")
        put("reach", "Reach")
        put("Velocity", "Velocity")
        put("AntiKick", "AntiKick")
        put("esp_module", "ESP")
        put("replay_module", "Replay Module")
        put("SpeedoMeter", "Speedometer")
        put("WaterMark", "Watermark")
        put("fullbright", "FullBright")
        put("text_spoof", "Text Spoof")
        put("autonavigator", "Auto Navigator")
        put("followbot", "Follow Bot")
        put("Minimap", "Minimap")









        put("times", "Times")
        put("flySpeed", "Fly Speed")
        put("range", "Range")
        put("cps", "CPS")
        put("amplifier", "Amplifier")
        put("nightVision", "Night Vision")
        put("scanRadius", "Scan Radius")
        put("jumpHeight", "Jump Height")
        put("verticalUpSpeed", "Vertical Up Speed")
        put("verticalDownSpeed", "Vertical Down Speed")
        put("motionInterval", "Motion Interval")
        put("glideSpeed", "Glide Speed")
        put("vanillaFly", "Vanilla Fly")
        put("repeat", "Repeat")
        put("delay", "Delay")
        put("enabled", "Enabled")
        put("disabled", "Disabled")
        put("players_only", "Players Only")
        put("mobs_only", "Mob Aura")
        put("time", "Time")
        put("keep_distance", "Distance")
        put("tp_speed", "Teleport Speed")
        put("packets", "Packets")
        put("strafe", "Strafe")
        put("tp_aura", "TP Aura")
        put("teleport_behind", "TP Behind")
        put("strafe_angle", "Strafe Angle")
        put("strafe_speed", "Strafe Speed")
        put("strafe_radius", "Strafe Radius")
        put("clear", "Clear")
        put("rain", "Rain")
        put("thunderstorm", "Thunderstorm")
        put("intensity", "Intensity")
        put("interval", "Interval")
        put("multi_target", "Multi")
        put("max_targets", "Max Targets")
        put("randomize_timing", "Random Timing")
        put("attack_packets", "Packets")
        put("walk_mode", "Walk Mode")
        put("adaptive_mode", "Adaptive Mode")
        put("tp_distance", "TP Distance")
        put("predictive_movement", "Predictive Movement")

    }



    fun getTranslationMap(language: String): Map<String, String> {
        val translationMap = map[language]
        if (translationMap != null) {
            return translationMap
        }

        map.forEach { (key, value) ->
            if (key.startsWith(language)) {
                return value
            }
        }

        return map["en"]!!
    }

}
