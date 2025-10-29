package com.project.lumina.client.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import java.io.InputStream
import com.project.lumina.client.R

class ThemeManager(context: Context) {
    private val json: JSONObject

    init {
        
        val inputStream: InputStream = context.resources.openRawResource(R.raw.theme)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        json = JSONObject(jsonString)
    }

    private fun hexToColor(hex: String?): Color {
        if (hex.isNullOrEmpty()) return Color.Unspecified
        val cleanHex = hex.removePrefix("#").trim()
        return when (cleanHex.length) {
            6 -> Color(android.graphics.Color.parseColor("#FF$cleanHex"))
            8 -> Color(android.graphics.Color.parseColor("#$cleanHex"))
            else -> Color.Unspecified
        }
    }

    private fun getColor(vararg keys: String): Color {
        var current: JSONObject? = json
        for (i in 0 until keys.size - 1) {
            current = current?.optJSONObject(keys[i]) ?: return Color.Unspecified
        }
        return hexToColor(current?.optString(keys.last()))
    }

    
    val primary: Color get() = getColor("main", "primary")
    val background: Color get() = getColor("main", "background")
    val surface: Color get() = getColor("main", "surface")
    val onBackground: Color get() = getColor("main", "onBackground")
    val onSurface: Color get() = getColor("main", "onSurface")
    val backgroundOverlayUi: Color get() = getColor("main", "backgroundOverlayUi")
    val backgroundOverlayUi2: Color get() = getColor("main", "backgroundOverlayUi2")
    val notBackgroundOverlayUi: Color get() = getColor("main", "notBackgroundOverlayUi")
    val textModules: Color get() = getColor("main", "textModules")

    
    val launcherRadial: Color get() = getColor("launchActivity", "launcherRadial")
    val lAnimation: Color get() = getColor("launchActivity", "lAnimation")
    val lText: Color get() = getColor("launchActivity", "lText")
    val lBlob1: Color get() = getColor("launchActivity", "lBlob1")
    val lBlob2: Color get() = getColor("launchActivity", "lBlob2")
    val lBg1: Color get() = getColor("launchActivity", "lBg1")
    val lBg2: Color get() = getColor("launchActivity", "lBg2")

    
    val mBg: Color get() = getColor("miniMap", "mBg")
    val mGrid: Color get() = getColor("miniMap", "mGrid")
    val mCrosshair: Color get() = getColor("miniMap", "mCrosshair")
    val mPlayerMarker: Color get() = getColor("miniMap", "mPlayerMarker")
    val mNorth: Color get() = getColor("miniMap", "mNorth")
    val mEntityClose: Color get() = getColor("miniMap", "mEntityClose")
    val mEntityFar: Color get() = getColor("miniMap", "mEntityFar")

    
    val oArrayList1: Color get() = getColor("arrayList", "oArrayList1")
    val oArrayList2: Color get() = getColor("arrayList", "oArrayList2")
    val oArrayBase: Color get() = getColor("arrayList", "oArrayBase")

    
    val oNotifAccent: Color get() = getColor("overlayNotification", "oNotifAccent")
    val oNotifBase: Color get() = getColor("overlayNotification", "oNotifBase")
    val oNotifText: Color get() = getColor("overlayNotification", "oNotifText")
    val oNotifProgressbar: Color get() = getColor("overlayNotification", "oNotifProgressbar")

    
    val pColorGradient1: Color get() = getColor("packetNotification", "pColorGradient1")
    val pColorGradient2: Color get() = getColor("packetNotification", "pColorGradient2")
    val pBackground: Color get() = getColor("packetNotification", "pBackground")

    
    val sBase: Color get() = getColor("sessionStats", "sBase")
    val sAccent: Color get() = getColor("sessionStats", "sAccent")
    val sBackgroundGradient1: Color get() = getColor("sessionStats", "sBackgroundGradient1")
    val sBackgroundGradient2: Color get() = getColor("sessionStats", "sBackgroundGradient2")

    
    val sMiniLineGraph: Color get() = getColor("speedoMeter", "sMiniLineGraph")
    val sMeterBg: Color get() = getColor("speedoMeter", "sMeterBg")
    val sMeterAccent: Color get() = getColor("speedoMeter", "sMeterAccent")
    val sMeterBase: Color get() = getColor("speedoMeter", "sMeterBase")

    
    val tcoGradient1: Color get() = getColor("topCenterOverlay", "tcoGradient1")
    val tcoGradient2: Color get() = getColor("topCenterOverlay", "tcoGradient2")
    val tcoBackground: Color get() = getColor("topCenterOverlay", "tcoBackground")

    
    val eColorCard1: Color get() = getColor("graceUi", "elevatedCard", "eColorCard1")
    val eColorCard2: Color get() = getColor("graceUi", "elevatedCard", "eColorCard2")
    val eColorCard3: Color get() = getColor("graceUi", "elevatedCard", "eColorCard3")
    val mColorCard1: Color get() = getColor("graceUi", "elevatedCard", "mColorCard1")
    val mColorCard2: Color get() = getColor("graceUi", "elevatedCard", "mColorCard2")
    val mColorCard3: Color get() = getColor("graceUi", "elevatedCard", "mColorCard3")
    val mColorScreen1: Color get() = getColor("graceUi", "moduleSettingScreen", "mColorScreen1")
    val mColorScreen2: Color get() = getColor("graceUi", "moduleSettingScreen", "mColorScreen2")
    val nColorItem1: Color get() = getColor("graceUi", "navigationRailItem", "nColorItem1")
    val nColorItem2: Color get() = getColor("graceUi", "navigationRailItem", "nColorItem2")
    val nColorItem3: Color get() = getColor("graceUi", "navigationRailItem", "nColorItem3")
    val nColorItem4: Color get() = getColor("graceUi", "navigationRailItem", "nColorItem4")
    val nColorItem5: Color get() = getColor("graceUi", "navigationRailItem", "nColorItem5")
    val nColorItem6: Color get() = getColor("graceUi", "navigationRailItem", "nColorItem6")
    val nColorItem7: Color get() = getColor("graceUi", "navigationRailItem", "nColorItem7")
    val pColorItem1: Color get() = getColor("graceUi", "packItem", "pColorItem1")

    
    val enabledBackground: Color get() = getColor("clickGui", "interfaceElement", "enabledBackground")
    val disabledBackground: Color get() = getColor("clickGui", "interfaceElement", "disabledBackground")
    val enabledGlow: Color get() = getColor("clickGui", "interfaceElement", "enabledGlow")
    val enabledText: Color get() = getColor("clickGui", "interfaceElement", "enabledText")
    val disabledText: Color get() = getColor("clickGui", "interfaceElement", "disabledText")
    val enabledIcon: Color get() = getColor("clickGui", "interfaceElement", "enabledIcon")
    val disabledIcon: Color get() = getColor("clickGui", "interfaceElement", "disabledIcon")
    val progressIndicator: Color get() = getColor("clickGui", "interfaceElement", "progressIndicator")
    val sliderTrack: Color get() = getColor("clickGui", "interfaceElement", "sliderTrack")
    val sliderActiveTrack: Color get() = getColor("clickGui", "interfaceElement", "sliderActiveTrack")
    val sliderThumb: Color get() = getColor("clickGui", "interfaceElement", "sliderThumb")
    val checkboxUnchecked: Color get() = getColor("clickGui", "interfaceElement", "checkboxUnchecked")
    val checkboxChecked: Color get() = getColor("clickGui", "interfaceElement", "checkboxChecked")
    val checkboxCheckmark: Color get() = getColor("clickGui", "interfaceElement", "checkboxCheckmark")
    val choiceSelected: Color get() = getColor("clickGui", "interfaceElement", "choiceSelected")
    val choiceUnselected: Color get() = getColor("clickGui", "interfaceElement", "choiceUnselected")



    
    val kitsuPrimary: Color get() = getColor("kitsu", "interfaceElement", "kitsuPrimary")
    val kitsuSecondary: Color get() = getColor("kitsu", "interfaceElement", "kitsuSecondary")
    val kitsuSurface: Color get() = getColor("kitsu", "interfaceElement", "kitsuSurface")
    val kitsuSurfaceVariant: Color get() = getColor("kitsu", "interfaceElement", "kitsuSurfaceVariant")
    val kitsuOnSurface: Color get() = getColor("kitsu", "interfaceElement", "kitsuOnSurface")
    val kitsuOnSurfaceVariant: Color get() = getColor("kitsu", "interfaceElement", "kitsuOnSurfaceVariant")
    val kitsuBackground: Color get() = getColor("kitsu", "interfaceElement", "kitsuBackground")
    val kitsuSelected: Color get() = getColor("kitsu", "interfaceElement", "kitsuSelected")
    val kitsuUnselected: Color get() = getColor("kitsu", "interfaceElement", "kitsuUnselected")
    val kitsuHover: Color get() = getColor("kitsu", "interfaceElement", "kitsuHover")

    //Keystrokes overlay
    val baseColor: Color get() = getColor("keystroke", "interfaceElement", "baseColor")
    val borderColor: Color get() = getColor("keystrokes", "interfaceElement", "borderColor")
    val pressedColor: Color get() = getColor("keystrokes", "interfaceElement", "pressedColor")
    val textColor: Color get() = getColor("keystrokes", "interfaceElement", "textColor")


    
    fun getMaterialColor(scheme: String, key: String): Color {
        return getColor("material", scheme, key)
    }

    fun getMaterialColorScheme(scheme: String): ColorScheme {
        return when (scheme) {
            "light" -> lightColorScheme(
                primary = getMaterialColor("light", "primary"),
                onPrimary = getMaterialColor("light", "onPrimary"),
                primaryContainer = getMaterialColor("light", "primaryContainer"),
                onPrimaryContainer = getMaterialColor("light", "onPrimaryContainer"),
                secondary = getMaterialColor("light", "secondary"),
                onSecondary = getMaterialColor("light", "onSecondary"),
                secondaryContainer = getMaterialColor("light", "secondaryContainer"),
                onSecondaryContainer = getMaterialColor("light", "onSecondaryContainer"),
                tertiary = getMaterialColor("light", "tertiary"),
                onTertiary = getMaterialColor("light", "onTertiary"),
                tertiaryContainer = getMaterialColor("light", "tertiaryContainer"),
                onTertiaryContainer = getMaterialColor("light", "onTertiaryContainer"),
                error = getMaterialColor("light", "error"),
                onError = getMaterialColor("light", "onError"),
                errorContainer = getMaterialColor("light", "errorContainer"),
                onErrorContainer = getMaterialColor("light", "onErrorContainer"),
                background = getMaterialColor("light", "background"),
                onBackground = getMaterialColor("light", "onBackground"),
                surface = getMaterialColor("light", "surface"),
                onSurface = getMaterialColor("light", "onSurface"),
                surfaceVariant = getMaterialColor("light", "surfaceVariant"),
                onSurfaceVariant = getMaterialColor("light", "onSurfaceVariant"),
                outline = getMaterialColor("light", "outline"),
                outlineVariant = getMaterialColor("light", "outlineVariant"),
                scrim = getMaterialColor("light", "scrim"),
                inverseSurface = getMaterialColor("light", "inverseSurface"),
                inverseOnSurface = getMaterialColor("light", "inverseOnSurface"),
                inversePrimary = getMaterialColor("light", "inversePrimary"),
                surfaceDim = getMaterialColor("light", "surfaceDim"),
                surfaceBright = getMaterialColor("light", "surfaceBright"),
                surfaceContainerLowest = getMaterialColor("light", "surfaceContainerLowest"),
                surfaceContainerLow = getMaterialColor("light", "surfaceContainerLow"),
                surfaceContainer = getMaterialColor("light", "surfaceContainer"),
                surfaceContainerHigh = getMaterialColor("light", "surfaceContainerHigh"),
                surfaceContainerHighest = getMaterialColor("light", "surfaceContainerHighest")
            )
            "dark" -> darkColorScheme(
                primary = getMaterialColor("dark", "primary"),
                onPrimary = getMaterialColor("dark", "onPrimary"),
                primaryContainer = getMaterialColor("dark", "primaryContainer"),
                onPrimaryContainer = getMaterialColor("dark", "onPrimaryContainer"),
                secondary = getMaterialColor("dark", "secondary"),
                onSecondary = getMaterialColor("dark", "onSecondary"),
                secondaryContainer = getMaterialColor("dark", "secondaryContainer"),
                onSecondaryContainer = getMaterialColor("dark", "onSecondaryContainer"),
                tertiary = getMaterialColor("dark", "tertiary"),
                onTertiary = getMaterialColor("dark", "onTertiary"),
                tertiaryContainer = getMaterialColor("dark", "tertiaryContainer"),
                onTertiaryContainer = getMaterialColor("dark", "onTertiaryContainer"),
                error = getMaterialColor("dark", "error"),
                onError = getMaterialColor("dark", "onError"),
                errorContainer = getMaterialColor("dark", "errorContainer"),
                onErrorContainer = getMaterialColor("dark", "onErrorContainer"),
                background = getMaterialColor("dark", "background"),
                onBackground = getMaterialColor("dark", "onBackground"),
                surface = getMaterialColor("dark", "surface"),
                onSurface = getMaterialColor("dark", "onSurface"),
                surfaceVariant = getMaterialColor("dark", "surfaceVariant"),
                onSurfaceVariant = getMaterialColor("dark", "onSurfaceVariant"),
                outline = getMaterialColor("dark", "outline"),
                outlineVariant = getMaterialColor("dark", "outlineVariant"),
                scrim = getMaterialColor("dark", "scrim"),
                inverseSurface = getMaterialColor("dark", "inverseSurface"),
                inverseOnSurface = getMaterialColor("dark", "inverseOnSurface"),
                inversePrimary = getMaterialColor("dark", "inversePrimary"),
                surfaceDim = getMaterialColor("dark", "surfaceDim"),
                surfaceBright = getMaterialColor("dark", "surfaceBright"),
                surfaceContainerLowest = getMaterialColor("dark", "surfaceContainerLowest"),
                surfaceContainerLow = getMaterialColor("dark", "surfaceContainerLow"),
                surfaceContainer = getMaterialColor("dark", "surfaceContainer"),
                surfaceContainerHigh = getMaterialColor("dark", "surfaceContainerHigh"),
                surfaceContainerHighest = getMaterialColor("dark", "surfaceContainerHighest")
            )
            else -> darkColorScheme(
                primary = getColor("main", "primary"),
                background = getColor("main", "background"),
                onBackground = getColor("main", "onBackground"),
                surface = getColor("main", "surface"),
                onSurface = getColor("main", "onSurface")
            )
        }
    }
}