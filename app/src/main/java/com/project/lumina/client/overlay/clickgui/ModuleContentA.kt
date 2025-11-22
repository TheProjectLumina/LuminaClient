package com.project.lumina.client.overlay.clickgui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import com.project.lumina.client.R
import com.project.lumina.client.constructors.BoolValue
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.FloatValue
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.constructors.IntValue
import com.project.lumina.client.constructors.ListValue
import com.project.lumina.client.overlay.manager.OverlayManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import com.project.lumina.client.ui.theme.*



private val moduleCache = HashMap<CheatCategory, List<Element>>()

private fun fetchCachedModules(cheatCategory: CheatCategory): List<Element> {
    val cachedModules = moduleCache[cheatCategory] ?: GameManager
        .elements
        .fastFilter { it.category === cheatCategory && it.name != "ChatListener" }
    moduleCache[cheatCategory] = cachedModules
    return cachedModules
}

@Composable
fun ModuleContentA(moduleCategory: CheatCategory) {
    var modules: List<Element>? by remember(moduleCategory) { mutableStateOf(moduleCache[moduleCategory]) }

    LaunchedEffect(modules) {
        if (modules == null) {
            withContext(Dispatchers.IO) {
                modules = fetchCachedModules(moduleCategory)
            }
        }
    }

    Crossfade(
        targetState = modules,
        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)
    ) {
        if (it != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(it.size) { index ->
                    val module = it[index]

                    
                    val animatedAlpha by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = index * 50,
                            easing = EaseInOutCubic
                        )
                    )

                    val animatedScale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                            visibilityThreshold = 0.01f
                        )
                    )

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = animatedAlpha
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }
                    ) {
                        ModuleCard(module)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                val rotationAnimation by animateFloatAsState(
                    targetValue = 360f,
                    animationSpec = tween(durationMillis = 1000, easing = EaseInOutCubic)
                )

                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp)
                        .graphicsLayer {
                            rotationZ = rotationAnimation
                        },
                    color = ProgressIndicatorColor
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(element: Element) {
    val values = element.values
    val hasSettings = values.isNotEmpty()

    val background by animateColorAsState(
        targetValue = if (element.isEnabled) EnabledBackgroundColor else DisabledBackgroundColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val cardScale by animateFloatAsState(
        targetValue = if (element.isEnabled) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (element.isEnabled) 0.3f else 0f,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .scale(cardScale)
            .drawWithContent {
                drawContent()
                if (element.isEnabled) {
                    drawRect(
                        color = EnabledGlowColor,
                        topLeft = this.center,
                        size = this.size,
                        alpha = glowAlpha
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    element.isEnabled = !element.isEnabled
                }
            ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(background)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(6.dp)
            ) {
                val textColor by animateColorAsState(
                    targetValue = if (element.isEnabled) EnabledTextColor else DisabledTextColor,
                    animationSpec = tween(durationMillis = 200)
                )

                Text(
                    modifier = Modifier
                        //.fillMaxWidth(),
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    text = element.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = MaterialTheme.typography.titleSmall.fontSize * 0.9
                    ),
                    color = textColor
                )

                Spacer(modifier = Modifier.weight(1f))
                if (hasSettings) {
                    val iconRotation by animateFloatAsState(
                        targetValue = if (element.isExpanded) 180f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (element.isExpanded) EnabledIconColor else DisabledIconColor,
                        animationSpec = tween(durationMillis = 200)
                    )

                    Icon(
                        painter = painterResource(
                             R.drawable.arrow_up
                        ),
                        contentDescription = if (element.isExpanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer {
                                rotationZ = iconRotation
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                element.isExpanded = !element.isExpanded
                            },
                        tint = iconTint
                    )
                }
            }
            if (element.isExpanded && hasSettings) {
                values.fastForEach {
                    when (it) {
                        is BoolValue -> BoolValueContent(it)
                        is FloatValue -> FloatValueContent(it)
                        is IntValue -> IntValueContent(it)
                        is ListValue -> ChoiceValueContent(it)
                        else -> {}
                    }
                }
                ShortcutContent(element)
            }
        }
    }
}

@Composable
private fun ChoiceValueContent(value: ListValue) {
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = 100)
    )

    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp, bottom = 3.dp)
            .alpha(contentAlpha)
    ) {
        Text(
            text = value.name,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.9),
            color = DisabledTextColor
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(top = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            value.listItems.forEach { item ->
                val isSelected = value.value == item

                val cardColor by animateColorAsState(
                    targetValue = if (isSelected) ChoiceSelectedColor else ChoiceUnselectedColor,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                val cardScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                Card(
                    modifier = Modifier
                        .height(20.dp)
                        .scale(cardScale)
                        .clickable {
                            if (value.value != item) {
                                value.value = item
                            }
                        },
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor,
                        contentColor = if (isSelected) EnabledTextColor else DisabledTextColor
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.9)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatValueContent(value: FloatValue) {
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = 150)
    )

    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp, bottom = 3.dp)
            .alpha(contentAlpha)
    ) {
        Row {
            Text(
                text = value.name,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.9),
                color = DisabledTextColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "%.2f".format(value.value),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.9),
                color = DisabledTextColor
            )
        }
        CustomSlider(
            value = value.value,
            valueRange = value.range,
            onValueChange = { newValue ->
                if (value.value != newValue) {
                    value.value = newValue 
                }
            },
            modifier = Modifier.height(20.dp)
        )
    }
}

@Composable
private fun IntValueContent(value: IntValue) {
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = 200)
    )

    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp, bottom = 3.dp)
            .alpha(contentAlpha)
    ) {
        Row {
            Text(
                text = value.name,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.9),
                color = DisabledTextColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value.value.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.9),
                color = DisabledTextColor
            )
        }
        CustomSlider(
            value = value.value.toFloat(),
            valueRange = value.range.toFloatRange(),
            onValueChange = { newValue ->
                val rounded = newValue.roundToInt()
                if (value.value != rounded) {
                    value.value = rounded
                }
            },
            modifier = Modifier.height(20.dp)
        )
    }
}

@Composable
private fun CustomSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sensitivity: Float = 2f
) {
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var localValue by remember(value) { mutableFloatStateOf(value) }

    
    LaunchedEffect(value) {
        if (!isDragging) {
            localValue = value
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .onGloballyPositioned { coordinates ->
                sliderWidth = coordinates.size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        localValue = value
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChange(localValue)
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (sliderWidth > 0) {
                            val fraction = (dragAmount * sensitivity) / sliderWidth
                            val range = valueRange.endInclusive - valueRange.start
                            val newValue = (localValue + fraction * range).coerceIn(valueRange)
                            localValue = newValue
                            onValueChange(newValue)
                        }
                    }
                )
            }
    ) {
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.Center)
                .background(SliderTrackColor, RoundedCornerShape(2.dp))
        )

        
        val fraction = (localValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        val trackWidth = (fraction * sliderWidth).coerceAtLeast(0f)

        Box(
            modifier = Modifier
                .width(trackWidth.toDp())
                .height(4.dp)
                .align(Alignment.CenterStart)
                .background(SliderActiveTrackColor, RoundedCornerShape(2.dp))
        )

        
        val thumbSize = if (isDragging) 14.dp else 12.dp

        Box(
            modifier = Modifier
                .offset(x = (fraction * sliderWidth).toDp() - (thumbSize / 2))
                .size(thumbSize)
                .background(SliderThumbColor, CircleShape)
                .align(Alignment.CenterStart)
        )
    }
}

@Composable
private fun BoolValueContent(value: BoolValue) {
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = 250)
    )

    Row(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp, bottom = 3.dp)
            .alpha(contentAlpha)
            .toggleable(
                value = value.value,
                interactionSource = null,
                indication = null,
                onValueChange = { value.value = it }
            )
    ) {
        Text(
            text = value.name,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.9),
            color = DisabledTextColor
        )
        Spacer(modifier = Modifier.weight(1f))

        val toggleScale by animateFloatAsState(
            targetValue = if (value.value) 1.1f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        val toggleColor by animateColorAsState(
            targetValue = if (value.value) CheckboxCheckedColor else CheckboxUncheckedColor,
            animationSpec = tween(durationMillis = 200)
        )

        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(toggleScale)
                .background(toggleColor, CircleShape)
        )
    }
}

@Composable
private fun ShortcutContent(element: Element) {
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = 300)
    )

    Row(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
            .alpha(contentAlpha)
            .toggleable(
                value = element.isShortcutDisplayed,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = {
                    element.isShortcutDisplayed = it
                    if (it) {
                        OverlayManager.showOverlayWindow(element.overlayShortcutButton)
                    } else {
                        OverlayManager.dismissOverlayWindow(element.overlayShortcutButton)
                    }
                }
            )
    ) {
        Text(
            text = stringResource(R.string.shortcut),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.9),
            color = DisabledTextColor
        )
        Spacer(modifier = Modifier.weight(1f))

        val toggleScale by animateFloatAsState(
            targetValue = if (element.isShortcutDisplayed) 1.1f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        val toggleColor by animateColorAsState(
            targetValue = if (element.isShortcutDisplayed) CheckboxCheckedColor else CheckboxUncheckedColor,
            animationSpec = tween(durationMillis = 200)
        )

        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(toggleScale)
                .background(toggleColor, CircleShape)
        )
    }
}

private fun IntRange.toFloatRange() = first.toFloat()..last.toFloat()

@Composable
private fun Float.toDp(): Dp = (this / LocalDensity.current.density).dp