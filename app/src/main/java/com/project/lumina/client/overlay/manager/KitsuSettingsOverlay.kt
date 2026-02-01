package com.project.lumina.client.overlay.manager

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.animateValueAsState
import com.project.lumina.client.constructors.BoolValue
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.FloatValue
import com.project.lumina.client.constructors.IntValue
import com.project.lumina.client.constructors.ListValue
import com.project.lumina.client.ui.theme.TheBackgroundColorForOverlayUi
import com.project.lumina.client.ui.theme.TheNotBackgroundColorForOverlayUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.content.Context
import androidx.compose.animation.core.SpringSpec

@Composable
fun KitsuSettingsOverlay(
    element: Element,
    onDismiss: () -> Unit
) {
    
    var shouldAnimate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val preferences = remember { context.getSharedPreferences("KitsuOverlayPrefs", Context.MODE_PRIVATE) }
    
    var isDragging by remember { mutableStateOf(false) }
    var dragVelocity by remember { mutableStateOf(Pair(0f, 0f)) }
    var lastDragTime by remember { mutableStateOf(0L) }
    var lastDragPosition by remember { mutableStateOf(Pair(0f, 0f)) }
    
    val savedX = remember { preferences.getFloat("${element.name}_PositionX", 0f) }
    val savedY = remember { preferences.getFloat("${element.name}_PositionY", 0f) }
    
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    var initialOffsetX by remember { mutableStateOf(savedX) }
    var initialOffsetY by remember { mutableStateOf(savedY) }
    
    val combinedOffset by animateValueAsState(
        targetValue = if (isDragging) {
            Pair(offsetX + initialOffsetX, offsetY + initialOffsetY)
        } else {
            Pair(initialOffsetX, initialOffsetY)
        },
        typeConverter = TupleConverter(),
        animationSpec = if (isDragging) {
            SpringSpec(stiffness = Spring.StiffnessHigh)
        } else {
            SpringSpec(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "offsetAnimation"
    )
    
    
    val dismissWithAnimation = remember {
        {
            shouldAnimate = false
            scope.launch {
                delay(300)
                onDismiss()
            }
            Unit
        }
    }
    
    val savePosition = remember {
        { x: Float, y: Float ->
            preferences.edit()
                .putFloat("${element.name}_PositionX", x)
                .putFloat("${element.name}_PositionY", y)
                .apply()
        }
    }
    
    
    LaunchedEffect(Unit) {
        shouldAnimate = true
    }
    
    
    val scale by animateFloatAsState(
        targetValue = if (shouldAnimate) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scaleAnimation"
    )
    
    
    val alpha by animateFloatAsState(
        targetValue = if (shouldAnimate) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "opacityAnimation"
    )
    
    
    val rotation by animateFloatAsState(
        targetValue = if (shouldAnimate) 0f else -3f,
        animationSpec = tween(durationMillis = 300),
        label = "rotationAnimation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha * 0.7f)
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { 
                dismissWithAnimation()
            }
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight(0.85f)
                .offset { IntOffset(combinedOffset.first.roundToInt(), combinedOffset.second.roundToInt()) }
                .scale(scale)
                .rotate(rotation)
                .alpha(alpha)
                .padding(top = 16.dp, bottom = 16.dp)
                .shadow(
                    elevation = 15.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.2f)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            isDragging = true
                            lastDragTime = System.currentTimeMillis()
                            lastDragPosition = Pair(0f, 0f)
                            dragVelocity = Pair(0f, 0f)
                        },
                        onDragEnd = {
                            isDragging = false
                            
                            initialOffsetX += offsetX
                            initialOffsetY += offsetY
                            
                            offsetX = 0f
                            offsetY = 0f
                            
                            savePosition(initialOffsetX, initialOffsetY)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            
                            
                            val currentTime = System.currentTimeMillis()
                            val timeDelta = currentTime - lastDragTime
                            
                            if (timeDelta > 0) {
                                val newVelocityX = dragAmount.x / timeDelta
                                val newVelocityY = dragAmount.y / timeDelta
                                
                                
                                dragVelocity = Pair(
                                    dragVelocity.first * 0.7f + newVelocityX * 0.3f,
                                    dragVelocity.second * 0.7f + newVelocityY * 0.3f
                                )
                                
                                lastDragTime = currentTime
                                lastDragPosition = Pair(dragAmount.x, dragAmount.y)
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = TheBackgroundColorForOverlayUi.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (element.displayNameResId != null) 
                            "${element.name} Settings" 
                        else 
                            "${element.name} Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = TheNotBackgroundColorForOverlayUi,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    
                    IconButton(
                        onClick = dismissWithAnimation,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TheNotBackgroundColorForOverlayUi
                        )
                    }
                }
                
                Divider(
                    color = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (element.values.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No settings available for this module",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        element.values.fastForEach { value ->
                            when (value) {
                                is BoolValue -> BoolValueSetting(value)
                                is FloatValue -> FloatValueSetting(value)
                                is IntValue -> IntValueSetting(value)
                                is ListValue -> ListValueSetting(value)
                                else -> {}
                            }
                            
                            Divider(
                                color = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.1f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = dismissWithAnimation,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Close",
                            color = TheNotBackgroundColorForOverlayUi
                        )
                    }
                }
            }
        }
    }
}

private class TupleConverter : androidx.compose.animation.core.TwoWayConverter<Pair<Float, Float>, AnimationVector2D> {
    override val convertToVector: (Pair<Float, Float>) -> AnimationVector2D = {
        AnimationVector2D(it.first, it.second)
    }
    
    override val convertFromVector: (AnimationVector2D) -> Pair<Float, Float> = {
        Pair(it.v1, it.v2)
    }
}

@Composable
private fun BoolValueSetting(value: BoolValue) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.name,
            style = MaterialTheme.typography.bodyMedium,
            color = TheNotBackgroundColorForOverlayUi
        )
        
        androidx.compose.material3.Switch(
            checked = value.value,
            onCheckedChange = { value.value = it },
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = TheNotBackgroundColorForOverlayUi,
                uncheckedThumbColor = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.6f),
                checkedTrackColor = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.2f),
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun FloatValueSetting(value: FloatValue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TheNotBackgroundColorForOverlayUi
            )
            
            Text(
                text = String.format("%.2f", value.value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TheNotBackgroundColorForOverlayUi
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        val interactionSource = remember { MutableInteractionSource() }
        
        androidx.compose.material3.Slider(
            value = value.value,
            onValueChange = { value.value = it },
            valueRange = value.range,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = TheNotBackgroundColorForOverlayUi,
                activeTrackColor = TheNotBackgroundColorForOverlayUi,
                inactiveTrackColor = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.2f)
            ),
            modifier = Modifier.height(20.dp)
        )
    }
}

@Composable
private fun IntValueSetting(value: IntValue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TheNotBackgroundColorForOverlayUi
            )
            
            Text(
                text = value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TheNotBackgroundColorForOverlayUi
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        val interactionSource = remember { MutableInteractionSource() }
        
        androidx.compose.material3.Slider(
            value = value.value.toFloat(),
            onValueChange = { value.value = it.toInt() },
            valueRange = value.range.first.toFloat()..value.range.last.toFloat(),
            steps = value.range.last - value.range.first - 1,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = TheNotBackgroundColorForOverlayUi,
                activeTrackColor = TheNotBackgroundColorForOverlayUi,
                inactiveTrackColor = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.2f)
            ),
            modifier = Modifier.height(20.dp)
        )
    }
}

@Composable
private fun ListValueSetting(value: ListValue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = value.name,
            style = MaterialTheme.typography.bodyMedium,
            color = TheNotBackgroundColorForOverlayUi
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            value.listItems.forEach { item ->
                val isSelected = value.value == item
                
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    onClick = { value.value = item },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) 
                        TheNotBackgroundColorForOverlayUi
                    else 
                        TheNotBackgroundColorForOverlayUi.copy(alpha = 0.1f),
                    shadowElevation = if (isSelected) 4.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.name,
                            color = if (isSelected) 
                                Color.Black
                            else
                                TheNotBackgroundColorForOverlayUi,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
} 