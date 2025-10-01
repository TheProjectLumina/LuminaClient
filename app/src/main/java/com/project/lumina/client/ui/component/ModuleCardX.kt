package io.havens.grace.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.project.lumina.client.ui.theme.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.project.lumina.client.constructors.Element


@Composable
fun ModuleCardX(
    element: Element,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.medium,
    onOpenSettings: () -> Unit
) {
    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(MColorCard1, MColorCard2),
            start = Offset.Zero,
            end = Offset(500f, 500f)
        )
    }





    Card(
        modifier = modifier
            .size(70.dp)


            .clickable(
                onClick = {
                    element.isEnabled = !element.isEnabled


                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (!element.isEnabled) MColorCard3 else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (element.isEnabled) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (element.isEnabled) {
                        Modifier.background(gradientBrush)
                    } else {
                        Modifier
                    }
                )
        ) {
            
            if (element.values.isNotEmpty()) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
            Text(
                text = if (element.displayNameResId != null) stringResource(id = element.displayNameResId!!) else element.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}