package com.project.lumina.client.overlay.wclient

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import kotlinx.coroutines.launch
import com.project.lumina.client.R
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.overlay.kitsugui.ModuleContent
import com.project.lumina.client.ui.component.ConfigCategoryContent
import kotlin.math.PI

class WClientUI : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            if (Build.VERSION.SDK_INT >= 31) {
                blurBehindRadius = 30
            }
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.8f
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedCheatCategory by mutableStateOf(CheatCategory.Combat)

    @Composable
    override fun Content() {
        val context = LocalContext.current

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x95000000),
                            Color(0xE0000000)
                        ),
                        radius = 1000f
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { OverlayManager.dismissOverlayWindow(this) },
            contentAlignment = Alignment.Center
        ) {
            
            Box(
                modifier = Modifier
                    .size(width = 720.dp, height = 480.dp)
                    .rgbBorder()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0A0A),
                                Color(0xFF151515),
                                Color(0xFF0A0A0A)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    
                    CompactHeader()

                    
                    MainContentArea()
                }
            }
        }
    }

    @Composable
    private fun CompactHeader() {
        val context = LocalContext.current

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xF53E3E3E), 
                            Color(0xE0404040), 
                            Color(0xEA363636), 
                            Color(0xF5262626)  
                        )
                    ),
                    RoundedCornerShape(15.dp)
                )
                .border(
                    1.5.dp,
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(15.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x40FFFFFF),
                                    Color(0x20FFFFFF)
                                )
                            ),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lumina),
                        contentDescription = "Lumina Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                RainbowText("Project Lumina", fontSize = 20f, fontWeight = FontWeight.Bold)
            }

            
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PremiumIconButton(
                    iconRes = R.drawable.ic_discord,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/N2Gejr8Fbp")))
                    }
                )
                PremiumIconButton(
                    iconRes = R.drawable.ic_web,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wclient.neocities.org/")))
                    }
                )
                PremiumIconButton(
                    iconRes = R.drawable.ic_settings,
                    onClick = { selectedCheatCategory = CheatCategory.Config }
                )
                PremiumIconButton(
                    iconRes = R.drawable.ic_close_black_24dp,
                    onClick = { OverlayManager.dismissOverlayWindow(this@WClientUI) }
                )
            }
        }
    }

    @Composable
    private fun MainContentArea() {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            CompactCategorySidebar()

            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x12FFFFFF),
                                Color(0x08FFFFFF),
                                Color(0x12FFFFFF)
                            )
                        ),
                        RoundedCornerShape(15.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = selectedCheatCategory,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 4 } togetherWith
                                fadeOut(animationSpec = tween(300)) + slideOutHorizontally { -it / 4 }
                    },
                    label = "CategoryContent"
                ) { category ->
                    if (category == CheatCategory.Config) {
                        ConfigCategoryContent()
                    } else {
                        ModuleContentP(category)
                    }
                }
            }
        }
    }

    @Composable
    private fun CompactCategorySidebar() {
        LazyColumn(
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x25FFFFFF),
                            Color(0x15FFFFFF),
                            Color(0x25FFFFFF)
                        )
                    ),
                    RoundedCornerShape(15.dp)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.12f),
                    RoundedCornerShape(15.dp)
                )
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(CheatCategory.entries.filter { it != CheatCategory.Home }.size) { index ->
                val category = CheatCategory.entries.filter { it != CheatCategory.Home }[index]
                CategoryIcon(
                    category = category,
                    isSelected = selectedCheatCategory == category,
                    onClick = { selectedCheatCategory = category }
                )
            }
        }
    }

    @Composable
    private fun CategoryIcon(
        category: CheatCategory,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val animatedScale by animateFloatAsState(
            targetValue = if (isSelected) 1.1f else 1f,
            animationSpec = spring(dampingRatio = 0.6f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(animatedScale)
                    .background(
                        if (isSelected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00FF88),
                                    Color(0xFF0088FF),
                                    Color(0xFF8800FF)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x35FFFFFF),
                                    Color(0x15FFFFFF)
                                )
                            )
                        },
                        CircleShape
                    )
                    .border(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(category.iconResId),
                    contentDescription = category.name,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = category.name,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(54.dp)
            )
        }
    }

    @Composable
    private fun PremiumIconButton(
        iconRes: Int,
        onClick: () -> Unit
    ) {
        val transition = rememberInfiniteTransition()
        val shimmer by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            )
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x30FFFFFF),
                            Color(0x15FFFFFF)
                        )
                    ),
                    CircleShape
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.2f + shimmer * 0.1f),
                    CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    @Composable
    private fun RainbowText(
        text: String,
        fontSize: Float,
        fontWeight: FontWeight = FontWeight.Normal
    ) {
        val transition = rememberInfiniteTransition()
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing)
            )
        )

        val colors = List(10) { i ->
            val hue = (i * 36 + phase) % 360
            Color.hsv(hue, 0.03f, 0.95f + (i % 2) * 0.03f)
        }

        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize.sp,
                fontWeight = fontWeight,
                brush = Brush.linearGradient(colors)
            )
        )
    }


    @Composable
    private fun PremiumActionCard(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        onClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x25FFFFFF),
                                Color(0x15FFFFFF)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x40FF0080),
                                        Color(0x4000FF80)
                                    )
                                ),
                                CircleShape
                            )
                            .border(1.dp, Color.White.copy(0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }

    
    @Composable
    private fun Modifier.rgbBorder(): Modifier {
        val transition = rememberInfiniteTransition()
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing)
            )
        )

        return this.drawBehind {
            val strokeWidth = 4.dp.toPx()
            val radius = 20.dp.toPx()

            
            val colors = listOf(
                Color.hsv((phase) % 360f, 0.05f, 0.95f),      
                Color.hsv((phase + 45) % 360f, 0.03f, 0.97f),  
                Color.hsv((phase + 90) % 360f, 0.05f, 0.93f),  
                Color.hsv((phase + 135) % 360f, 0.04f, 0.96f), 
                Color.hsv((phase + 180) % 360f, 0.05f, 0.94f), 
                Color.hsv((phase + 225) % 360f, 0.03f, 0.98f), 
                Color.hsv((phase + 270) % 360f, 0.05f, 0.95f), 
                Color.hsv((phase + 315) % 360f, 0.04f, 0.97f), 
                Color.hsv((phase) % 360f, 0.05f, 0.95f)       
            )

            val brush = Brush.sweepGradient(colors)

            drawRoundRect(
                brush = brush,
                style = Stroke(width = strokeWidth),
                cornerRadius = CornerRadius(radius)
            )
        }
    }
}