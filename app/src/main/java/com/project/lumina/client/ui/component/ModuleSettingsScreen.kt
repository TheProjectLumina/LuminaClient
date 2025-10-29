package com.project.lumina.client.ui.component



import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.project.lumina.client.ui.theme.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import coil.compose.AsyncImagePainter.State.Empty.painter
import com.project.lumina.client.R
import com.project.lumina.client.constructors.BoolValue
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.FloatValue
import com.project.lumina.client.constructors.IntValue
import com.project.lumina.client.constructors.ListValue
import com.project.lumina.client.overlay.grace.BoolValueContent
import com.project.lumina.client.overlay.grace.ChoiceValueContent
import com.project.lumina.client.overlay.grace.FloatValueContent
import com.project.lumina.client.overlay.grace.IntValueContent
import com.project.lumina.client.overlay.grace.ShortcutContent

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ModuleSettingsScreen(
    element: Element,
    onDismiss: () -> Unit
) {


    
    var shouldAnimate by remember { mutableStateOf(false) }

    
    LaunchedEffect(Unit) {
        shouldAnimate = true
    }

    
    val offsetX by animateDpAsState(
        targetValue = if (shouldAnimate) 0.dp else (-500).dp,
        animationSpec = tween(durationMillis = 300),
        label = "slideAnimation"
    )

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .wrapContentHeight()
                .padding(16.dp)
                .offset(x = offsetX)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MColorScreen1, MColorScreen1),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            Surface(
                onClick = onDismiss,
                color = Color.Transparent,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Column( 
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp) 
                    ) {
                        Text(
                            "Artifact Settings",
                            modifier = Modifier.align(Alignment.CenterStart),
                            style = MaterialTheme.typography.titleMedium,
                            color = MColorScreen2.copy(alpha = 0.9f)
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterEnd) 
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close_black_24dp),
                                contentDescription = "Close",
                                tint = MColorScreen2.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        element.values.fastForEach {
                            when (it) {
                                is BoolValue -> BoolValueContent(it)
                                is FloatValue -> FloatValueContent(it)
                                is IntValue -> IntValueContent(it)
                                is ListValue -> ChoiceValueContent(it)
                                else -> {}
                            }
                        }

                        ShortcutContent(element)

                        Spacer(Modifier.height(4.dp))

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MColorScreen2.copy(alpha = 0.9f)
                            )
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
