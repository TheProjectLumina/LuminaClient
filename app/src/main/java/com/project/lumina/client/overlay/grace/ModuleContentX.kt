package com.project.lumina.client.overlay.grace

import io.havens.grace.ui.component.ModuleCardX
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import com.project.lumina.client.R
import com.project.lumina.client.constructors.BoolValue
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.FloatValue
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.constructors.IntValue
import com.project.lumina.client.constructors.ListValue
import com.project.lumina.client.overlay.manager.OverlayManager
import com.smarttoolfactory.slider.ColorfulSlider
import com.smarttoolfactory.slider.MaterialSliderDefaults
import com.smarttoolfactory.slider.SliderBrushColor
import kotlin.math.roundToInt

private val artifactCache = HashMap<CheatCategory, List<Element>>()

private fun fetchCachedModules(TBCategory: CheatCategory): List<Element> {
    val cachedModules = artifactCache[TBCategory] ?: GameManager
        .elements
        .fastFilter { it.category === TBCategory && it.name != "ChatListener" }
    artifactCache[TBCategory] = cachedModules
    return cachedModules
}

@Composable
fun ModuleContentX(TBCategory: CheatCategory, onOpenSettings: (Element) -> Unit) {
    var artifacts: List<Element>? by remember(TBCategory) { mutableStateOf(artifactCache[TBCategory]) }

    if (artifacts == null) {
        artifacts = fetchCachedModules(TBCategory)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 90.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(artifacts.orEmpty().size) { index ->
            val module = artifacts!![index]
            ModuleCardX(
                element = module,
                onOpenSettings = { onOpenSettings(module) }
            )
        }
    }
}


@Composable
internal fun ChoiceValueContent(value: ListValue) {
    Column(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
    ) {
        Text(
            value.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCECECE)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(value.listItems.toList()) { item ->
                ElevatedFilterChip(
                    selected = value.value == item,
                    onClick = {
                        if (value.value != item) {
                            value.value = item
                        }
                    },
                    label = { Text(text = item.name) },
                    modifier = Modifier.height(30.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFFCECECE),
                        selectedContainerColor = Color(0xFF000000),
                        selectedLabelColor = Color(0xFFCECECE)
                    )
                )
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    ColorfulSlider(
        value = value,
        onValueChange = { newValue: Float -> onValueChange(newValue) },
        valueRange = valueRange,
        trackHeight = 12.dp,
        thumbRadius = 6.dp,
        modifier = modifier,
        colors = MaterialSliderDefaults.materialColors(
            inactiveTrackColor = SliderBrushColor(color = Color.Transparent),
            activeTrackColor = SliderBrushColor(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF85FB6), Color(0xFF8EFAFA), Color(0xFF439CFB))
                )
            )
        ),
        borderStroke = BorderStroke(2.dp, Color(0xFFadadad)),
        drawInactiveTrack = true
    )
}



@Composable
internal fun FloatValueContent(value: FloatValue) {
    Column(
        Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
    ) {
        Row {
            Text(
                value.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCECECE)
            )
            Spacer(Modifier.weight(1f))
            Text(
                "%.1f".format(value.value),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCECECE)
            )
        }

        CustomSlider(
            value = value.value,
            onValueChange = { newValue -> value.value = newValue },
            valueRange = value.range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IntValueContent(value: IntValue) {
    Column(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
    ) {
        Row {
            Text(
                value.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCECECE)
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCECECE)
            )
        }
        CustomSlider(
            value = value.value.toFloat(),
            onValueChange = { newValue -> value.value = newValue.roundToInt() },
            valueRange = value.range.toFloatRange(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun BoolValueContent(value: BoolValue) {
    Row(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            .toggleable(
                value = value.value,
                interactionSource = null,
                indication = null,
                onValueChange = {
                    value.value = it
                }
            )
    ) {
        Text(
            value.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCECECE)
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = value.value,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = Color(0xFFCECECE),
                checkedColor = Color(0xFFCECECE),
                checkmarkColor = Color(0xFFCECECE)
            )
        )
    }
}

@Composable
internal fun ShortcutContent(element: Element) {
    Row(
        Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
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
            stringResource(R.string.shortcut),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCECECE)
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = element.isShortcutDisplayed,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = Color(0xFFCECECE),
                checkedColor = Color(0xFFCECECE),
                checkmarkColor = Color(0xFFCECECE)
            )
        )
    }
}

private fun IntRange.toFloatRange() = first.toFloat()..last.toFloat()