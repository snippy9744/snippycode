package com.snippyseat.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-offset",
    )

    Canvas(modifier = modifier.fillMaxWidth()) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFEDEDED),
                    Color(0xFFF8F8F8),
                    Color(0xFFEDEDED),
                ),
                start = Offset(shimmerOffset, 0f),
                end = Offset(shimmerOffset + size.width, size.height),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
        )
    }
}
