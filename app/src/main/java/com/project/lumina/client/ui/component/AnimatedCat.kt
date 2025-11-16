package com.project.lumina.client.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.project.lumina.client.R

@Composable
fun AnimatedCat(
    modifier: Modifier = Modifier,
    size: Int = 56
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.animated_cat)
    )
    
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier.size(size.dp)
    )
}