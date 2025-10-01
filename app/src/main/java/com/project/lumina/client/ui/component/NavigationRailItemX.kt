package com.project.lumina.client.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.project.lumina.client.ui.theme.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun NavigationRailItemX(
    selected: Boolean,
    onClick: () -> Unit,
    iconResId: Int,
    labelResId: Int,
    modifier: Modifier = Modifier
) {
    
    val transition = rememberInfiniteTransition()
    val animatedOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 200f, 
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 1.dp)
            .clickable(onClick = onClick)
            .height(50.dp)
            .fillMaxWidth(0.95f)
            .background(
                brush = if (selected) {
                    Brush.linearGradient(
                        colors = listOf(
                            NColorItem1, 
                            NColorItem1, 
                            NColorItem1  
                        ),
                        start = Offset(animatedOffset, animatedOffset),
                        end = Offset(
                            animatedOffset + 400f,
                            animatedOffset + 400f
                        ) 
                    )
                } else {
                    SolidColor(Color.Transparent) 
                },
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            
            Box(modifier = Modifier.weight(1f))

            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = if (selected) NColorItem2 else NColorItem3,
                    modifier = Modifier.size(20.dp)
                )
            }

            
            Box(modifier = Modifier.width(12.dp))

            
            Text(
                text = stringResource(labelResId),
                color = if (selected) NColorItem2 else NColorItem3,
                //fontFamily = CustomFontFamily,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                textAlign = TextAlign.Start
            )

            
            Box(modifier = Modifier.weight(1f))
        }
    }
}