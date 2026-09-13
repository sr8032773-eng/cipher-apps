package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaViolet

@Composable
fun LiveWaveformBar(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 36.dp,
    barWidth: Dp = 3.dp,
    isPlaying: Boolean = true,
    activeColor: Color = RaagaAmber,
    inactiveColor: Color = Color.Gray.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        amplitudes.forEachIndexed { index, amp ->
            val animatedHeight by animateFloatAsState(
                targetValue = if (isPlaying) amp.coerceIn(0.12f, 1.0f) else 0.15f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                label = "BarHeight$index"
            )

            val currentHeight = maxHeight * animatedHeight

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(currentHeight.coerceAtLeast(3.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPlaying) {
                            Brush.verticalGradient(
                                listOf(RaagaViolet, activeColor)
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(inactiveColor, inactiveColor)
                            )
                        }
                    )
            )
        }
    }
}
