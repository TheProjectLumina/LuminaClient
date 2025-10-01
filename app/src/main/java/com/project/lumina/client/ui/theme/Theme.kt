package com.project.lumina.client.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.project.lumina.client.R
import com.project.lumina.client.application.AppContext



val primary = AppContext.themeManager.primary
val BackgroundColor = AppContext.themeManager.background
val SurfaceColor = AppContext.themeManager.surface
val OnBackgroundColor = AppContext.themeManager.onBackground
val OnSurfaceColor = AppContext.themeManager.onSurface
val TheBackgroundColorForOverlayUi = AppContext.themeManager.backgroundOverlayUi
val TheBackgroundColorForOverlayUi2 = AppContext.themeManager.backgroundOverlayUi2
val TheNotBackgroundColorForOverlayUi = AppContext.themeManager.notBackgroundOverlayUi
val TextColorForModules = AppContext.themeManager.textModules


val LauncherRadialColor = AppContext.themeManager.launcherRadial
val LAnimationColor = AppContext.themeManager.lAnimation
val LTextColor = AppContext.themeManager.lText
val LBlobColor1 = AppContext.themeManager.lBlob1
val LBlobColor2 = AppContext.themeManager.lBlob2
val LBg1 = AppContext.themeManager.lBg1
val LBg2 = AppContext.themeManager.lBg2


val Mbg = AppContext.themeManager.mBg
val MgridColor = AppContext.themeManager.mGrid
val MCrosshair = AppContext.themeManager.mCrosshair
val MPlayerMarker = AppContext.themeManager.mPlayerMarker
val MNorth = AppContext.themeManager.mNorth
val MEntityClose = AppContext.themeManager.mEntityClose
val MEntityFar = AppContext.themeManager.mEntityFar


val OArrayList1 = AppContext.themeManager.oArrayList1
val OArrayList2 = AppContext.themeManager.oArrayList2
val OArrayBase = AppContext.themeManager.oArrayBase


val ONotifAccent = AppContext.themeManager.oNotifAccent
val ONotifBase = AppContext.themeManager.oNotifBase
val ONotifText = AppContext.themeManager.oNotifText
val ONotifProgressbar = AppContext.themeManager.oNotifProgressbar


val PColorGradient1 = AppContext.themeManager.pColorGradient1
val PColorGradient2 = AppContext.themeManager.pColorGradient2
val PBackground = AppContext.themeManager.pBackground


val SBaseColor = AppContext.themeManager.sBase
val SAccentColor = AppContext.themeManager.sAccent
val SBAckgroundGradient1 = AppContext.themeManager.sBackgroundGradient1
val SBAckgroundGradient2 = AppContext.themeManager.sBackgroundGradient2


val SMiniLineGrpah = AppContext.themeManager.sMiniLineGraph
val SMeterBg = AppContext.themeManager.sMeterBg
val SMeterAccent = AppContext.themeManager.sMeterAccent
val SMeterBase = AppContext.themeManager.sMeterBase


val TCOGradient1 = AppContext.themeManager.tcoGradient1
val TCOGradient2 = AppContext.themeManager.tcoGradient2
val TCOBackground = AppContext.themeManager.tcoBackground


val EColorCard1 = AppContext.themeManager.eColorCard1
val EColorCard2 = AppContext.themeManager.eColorCard2
val EColorCard3 = AppContext.themeManager.eColorCard3
val MColorCard1 = AppContext.themeManager.mColorCard1
val MColorCard2 = AppContext.themeManager.mColorCard2
val MColorCard3 = AppContext.themeManager.mColorCard3
val MColorScreen1 = AppContext.themeManager.mColorScreen1
val MColorScreen2 = AppContext.themeManager.mColorScreen2
val NColorItem1 = AppContext.themeManager.nColorItem1
val NColorItem2 = AppContext.themeManager.nColorItem2
val NColorItem3 = AppContext.themeManager.nColorItem3
val NColorItem4 = AppContext.themeManager.nColorItem4
val NColorItem5 = AppContext.themeManager.nColorItem5
val NColorItem6 = AppContext.themeManager.nColorItem6
val NColorItem7 = AppContext.themeManager.nColorItem7
val PColorItem1 = AppContext.themeManager.pColorItem1




val EnabledBackgroundColor = AppContext.themeManager.enabledBackground
val DisabledBackgroundColor = AppContext.themeManager.disabledBackground
val EnabledGlowColor = AppContext.themeManager.enabledGlow
val EnabledTextColor = AppContext.themeManager.enabledText
val DisabledTextColor = AppContext.themeManager.disabledText
val EnabledIconColor = AppContext.themeManager.enabledIcon
val DisabledIconColor = AppContext.themeManager.disabledIcon
 val ProgressIndicatorColor = AppContext.themeManager.progressIndicator
 val SliderTrackColor = AppContext.themeManager.sliderTrack
val SliderActiveTrackColor = AppContext.themeManager.sliderActiveTrack
val SliderThumbColor = AppContext.themeManager.sliderThumb
 val CheckboxUncheckedColor = AppContext.themeManager.checkboxUnchecked
 val CheckboxCheckedColor = AppContext.themeManager.checkboxChecked
 val CheckboxCheckmarkColor = AppContext.themeManager.checkboxCheckmark
 val ChoiceSelectedColor = AppContext.themeManager.choiceSelected
 val ChoiceUnselectedColor = AppContext.themeManager.choiceUnselected

//KitsuGui
val KitsuPrimary = AppContext.themeManager.kitsuPrimary
val KitsuSecondary = AppContext.themeManager.kitsuSecondary
val KitsuSurface = AppContext.themeManager.kitsuSurface
val KitsuSurfaceVariant = AppContext.themeManager.kitsuSurfaceVariant
val KitsuOnSurface = AppContext.themeManager.kitsuOnSurface
val KitsuOnSurfaceVariant = AppContext.themeManager.kitsuOnSurfaceVariant
val KitsuBackground = AppContext.themeManager.kitsuBackground
val KitsuSelected = AppContext.themeManager.kitsuSelected
val KitsuUnselected = AppContext.themeManager.kitsuUnselected
val KitsuHover = AppContext.themeManager.kitsuHover

//Keystrokes Overlay

val baseColor = AppContext.themeManager.baseColor
val borderColor = AppContext.themeManager.borderColor
val pressedColor = AppContext.themeManager.pressedColor
val textColor = AppContext.themeManager.textColor

val MyFontFamily = FontFamily(
    Font(R.font.fredoka_light)
)

val MyTypography = Typography(
    displayLarge = TextStyle(fontFamily = MyFontFamily, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = MyFontFamily, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = MyFontFamily, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = MyFontFamily, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = MyFontFamily, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = MyFontFamily, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = MyFontFamily, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = MyFontFamily, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = MyFontFamily, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = MyFontFamily, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = MyFontFamily, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = MyFontFamily, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = MyFontFamily, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = MyFontFamily, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = MyFontFamily, fontSize = 11.sp),
)
@Composable
fun LuminaClientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AppContext.themeManager.getMaterialColorScheme("dark")
        else -> AppContext.themeManager.getMaterialColorScheme("dark")
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MyTypography,
        content = content
    )
}