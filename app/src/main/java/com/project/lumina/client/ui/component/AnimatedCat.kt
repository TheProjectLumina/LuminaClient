package com.project.lumina.client.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.project.lumina.client.R

@Composable
fun AnimatedCat(
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    Image(
        painter = painterResource(id = R.drawable.animated_cat),
        contentDescription = "Animated Cat",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size.dp)
    )
}
