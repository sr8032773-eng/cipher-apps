package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GradientAmber1
import com.example.ui.theme.GradientAmber2
import com.example.ui.theme.GradientForest1
import com.example.ui.theme.GradientForest2
import com.example.ui.theme.GradientOcean1
import com.example.ui.theme.GradientOcean2
import com.example.ui.theme.GradientSunset1
import com.example.ui.theme.GradientSunset2
import com.example.ui.theme.GradientViolet1
import com.example.ui.theme.GradientViolet2
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaViolet

val AlbumGradients = listOf(
    listOf(GradientSunset1, GradientSunset2),
    listOf(GradientViolet1, GradientViolet2),
    listOf(GradientOcean1, GradientOcean2),
    listOf(GradientForest1, GradientForest2),
    listOf(GradientAmber1, GradientAmber2),
    listOf(RaagaAmber, RaagaViolet)
)

@Composable
fun AlbumArtCard(
    gradientKey: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    shapeRadius: Dp = 16.dp,
    showVinylOverlay: Boolean = false,
    isPlaying: Boolean = false
) {
    val colors = AlbumGradients.getOrElse(gradientKey % AlbumGradients.size) {
        listOf(RaagaAmber, RaagaViolet)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "VinylSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VinylRotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(12.dp, RoundedCornerShape(shapeRadius), spotColor = colors.first().copy(alpha = 0.5f))
            .clip(RoundedCornerShape(shapeRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative geometric waveform backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Concentric soundwave circles
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = w * 0.45f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = w * 0.32f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = w * 0.20f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
        }

        if (showVinylOverlay) {
            // Vinyl Record Center Piece
            Box(
                modifier = Modifier
                    .size(size * 0.82f)
                    .clip(CircleShape)
                    .background(Color(0xFF111115))
                    .border(2.dp, Color(0xFF2A2A35), CircleShape)
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl grooves
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val cx = w / 2f
                    val cy = w / 2f
                    listOf(0.85f, 0.72f, 0.60f, 0.48f, 0.38f).forEach { scale ->
                        drawCircle(
                            color = Color(0x33FFFFFF),
                            radius = cx * scale,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
                        )
                    }
                }

                // Vinyl Center Label
                Box(
                    modifier = Modifier
                        .size(size * 0.30f)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(colors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(size * 0.08f)
                            .clip(CircleShape)
                            .background(Color(0xFF0C0E14))
                    )
                }
            }
        } else {
            // Compact emblem
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(size * 0.42f)
            )
        }
    }
}
