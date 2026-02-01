package com.project.lumina.client.overlay.manager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.R
import com.project.lumina.client.chat.ChatCategoryContent
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.overlay.kitsugui.ModuleContent
import com.project.lumina.client.ui.component.ConfigCategoryContent
import com.project.lumina.client.ui.component.NavigationRailItemX
import com.project.lumina.client.ui.theme.TheBackgroundColorForOverlayUi
import com.project.lumina.client.ui.theme.TheBackgroundColorForOverlayUi2
import com.project.lumina.client.ui.theme.TheNotBackgroundColorForOverlayUi


class OverlayClickGUI : OverlayWindow() {

    companion object {
        const val FILE_PICKER_REQUEST_CODE = 1001
    }

    val pretzelsuwu = FontFamily(
        Font(R.font.fredoka_light)
    )

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

            if (Build.VERSION.SDK_INT >= 31) {
                blurBehindRadius = 15
            }

            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

            dimAmount = 0.4f
            windowAnimations = 0 
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedCheatCategory by mutableStateOf(CheatCategory.Motion)

    @Composable
    override fun Content() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x10FFFFFF))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    OverlayManager.dismissOverlayWindow(this)
                },
            contentAlignment = Alignment.Center
        ) {
            
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TheBackgroundColorForOverlayUi
                ),
                modifier = Modifier
                    .width(582.dp)
                    .height(345.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
                    .border(
                        width = 1.5.dp,
                        color = TheNotBackgroundColorForOverlayUi,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(
                        shape = RoundedCornerShape(20.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    Surface(
                        color = TheBackgroundColorForOverlayUi2,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .height(270.dp)
                            .width(180.dp)
                            .padding(top = 0.dp)
                            .border(
                                width = 1.dp,
                                color = TheNotBackgroundColorForOverlayUi,
                                shape = RoundedCornerShape(
                                    topEnd = 20.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp
                                )
                            )
                            .clip(
                                RoundedCornerShape(
                                    topEnd = 20.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 8.dp)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            CheatCategory.entries.forEach { category ->
                                NavigationRailItemX(
                                    selected = selectedCheatCategory == category,
                                    onClick = { selectedCheatCategory = category },
                                    iconResId = category.iconResId,
                                    labelResId = category.labelResId,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .width(400.dp)
                            .height(270.dp)
                            .padding(start = 8.dp, end = 4.dp, bottom = 4.dp)
                    ) {
                        
                        when (selectedCheatCategory) {
                            CheatCategory.Config -> ConfigCategoryContent()
                            CheatCategory.Chat -> ChatCategoryContent()
                            else -> ModuleContent(selectedCheatCategory)
                        }
                    }

                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 13.dp, end = 13.dp)
                            .size(40.dp)
                            .clickable { OverlayManager.dismissOverlayWindow(this@OverlayClickGUI) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cross_circle_24),
                            contentDescription = "Close",
                            tint = TheNotBackgroundColorForOverlayUi,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 60.dp)
                            .width(190.dp)
                            .height(50.dp)
                            .background(
                                color = Color(0x00110011).copy(alpha = 0f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = TheNotBackgroundColorForOverlayUi,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = LocalContext.current
                        
                        Icon(
                            painter = painterResource(id = R.drawable.discord_24),
                            contentDescription = "Discord",
                            tint = TheNotBackgroundColorForOverlayUi,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { 
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/6kz3dcndrN")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.browser_24),
                            contentDescription = "Help",
                            tint = TheNotBackgroundColorForOverlayUi,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { 
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://projectlumina.netlify.app/")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.circle_book_open_24),
                            contentDescription = "Trigger_Gui",
                            tint = TheNotBackgroundColorForOverlayUi,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { 
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TheProjectLumina/LuminaClient")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                        )
                    }

                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 12.dp, start = 10.dp)
                            .width(250.dp)
                            .height(50.dp)
                            .background(
                                color = Color(0x00110011).copy(alpha = 0f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = TheNotBackgroundColorForOverlayUi,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.moon_stars_24),
                            contentDescription = "Logo",
                            tint = TheNotBackgroundColorForOverlayUi,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(top = 0.dp, bottom = 0.dp, start = 25.dp, end = 0.dp)
                        )
                        Text(
                            text = "L U M I N A",
                            style = TextStyle(
                                fontSize = 23.sp,
                                fontFamily = pretzelsuwu,
                                fontWeight = FontWeight.Thin,
                                color = TheNotBackgroundColorForOverlayUi
                            ),
                            modifier = Modifier
                                .padding(top = 2.dp, bottom = 2.dp, start = 30.dp, end = 2.dp)
                        )
                    }
                }
            }
        }
    }

}