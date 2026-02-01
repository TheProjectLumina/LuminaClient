package com.project.lumina.client.overlay.grace
import AnimatedContentX
import ElevatedCardX
import com.project.lumina.client.ui.component.ModuleSettingsScreen
import NavigationRailItemY
import NavigationRailY
import com.project.lumina.client.R
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBarDefaults.windowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.component.ConfigCategoryContent

class GraceMenuUi : OverlayWindow() {


    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags =
                flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            if (Build.VERSION.SDK_INT >= 31) {
                blurBehindRadius = 15
            }

            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

            dimAmount = 0.4f
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedCheatCategory by mutableStateOf(CheatCategory.Motion)
    private var selectedArtifact by mutableStateOf<Element?>(null) 
    private var isSettingsOpen by mutableStateOf(false) 

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    OverlayManager.dismissOverlayWindow(this)
                },

            contentAlignment = Alignment.Center
        ) {
            
            ElevatedCardX {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {


                    val gradientBrush = remember {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6B48FF), Color(0xFF00DDEB)),
                            start = Offset.Zero,
                            end = Offset(100f, 100f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(70.dp)
                            .windowInsetsPadding(windowInsets),
                        contentAlignment = Alignment.Center 
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            NavigationRailY(
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                modifier = Modifier
                                    //.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.moon_stars_24),
                                    contentDescription = "LOGO",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .scale(0.8f, 0.8f)
                                )
                                CheatCategory.entries
                                    .filter { it != CheatCategory.Home }
                                    .forEach { category ->
                                    NavigationRailItemY(
                                        selected = selectedCheatCategory == category,
                                        onClick = { selectedCheatCategory = category },
                                        iconResId = category.iconResId,
                                        labelResId = category.labelResId,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }

                            }
                        }

                    }
                    AnimatedContentX(
                        targetState = selectedCheatCategory,
                        modifier = Modifier
                            
                            .fillMaxSize() 
                    ) { selectedCheatCategory ->

                        if (selectedCheatCategory == com.project.lumina.client.constructors.CheatCategory.Config) {
                            ConfigCategoryContent()
                        } else {

                            ModuleContentX(
                                selectedCheatCategory,
                                onOpenSettings = { module ->
                                    selectedArtifact = module
                                    isSettingsOpen = true 
                                }
                            )
                        }
                    }

                }
            }


            
            if (isSettingsOpen && selectedArtifact != null) {

                ModuleSettingsScreen(


                    element = selectedArtifact!!,
                    onDismiss = { isSettingsOpen = false }
                )
            }
        }
    }


}
