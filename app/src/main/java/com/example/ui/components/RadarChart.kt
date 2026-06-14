package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.ui.viewmodel.DisciplyStats
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarChart(
    stats: DisciplyStats,
    modifier: Modifier = Modifier
) {
    val bentoTextSecondaryColor = BentoTextSecondary
    val labels = listOf("Focus (Weekday)", "Momentum (Recent)", "Grit (Weekend)", "Volume (Density)", "Streak (Current)")

    val animFocus = remember { Animatable(0f) }
    val animMomentum = remember { Animatable(0f) }
    val animGrit = remember { Animatable(0f) }
    val animVolume = remember { Animatable(0f) }
    val animStreak = remember { Animatable(0f) }

    LaunchedEffect(stats) {
        animFocus.animateTo(stats.focusScore, animationSpec = tween(durationMillis = 1000))
    }
    LaunchedEffect(stats) {
        animMomentum.animateTo(stats.momentumScore, animationSpec = tween(durationMillis = 1000))
    }
    LaunchedEffect(stats) {
        animGrit.animateTo(stats.gritScore, animationSpec = tween(durationMillis = 1000))
    }
    LaunchedEffect(stats) {
        animVolume.animateTo(stats.volumeScore, animationSpec = tween(durationMillis = 1000))
    }
    LaunchedEffect(stats) {
        animStreak.animateTo(stats.streakScore, animationSpec = tween(durationMillis = 1000))
    }

    val animatedValues = listOf(
        animFocus.value,
        animMomentum.value,
        animGrit.value,
        animVolume.value,
        animStreak.value
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(BentoSurfaceMuted)
            .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DISCIPLINE RADAR",
            color = BentoTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width.coerceAtMost(size.height) * 0.38f
                val numAxes = 5
                val startAngleRad = Math.toRadians(-90.0) // Star at 12 o'clock

                // 1. Draw web concentric pentagons
                val levels = 5
                val levelColor = bentoTextSecondaryColor.copy(alpha = 0.15f)
                for (j in 1..levels) {
                    val r = maxRadius * (j.toFloat() / levels)
                    val bgPath = Path()
                    for (i in 0 until numAxes) {
                        val angle = startAngleRad + i * (2 * Math.PI / numAxes)
                        val x = center.x + r * cos(angle).toFloat()
                        val y = center.y + r * sin(angle).toFloat()
                        if (i == 0) bgPath.moveTo(x, y) else bgPath.lineTo(x, y)
                    }
                    bgPath.close()
                    drawPath(bgPath, color = levelColor, style = Stroke(width = 1.dp.toPx()))
                }

                // 2. Draw axes lines
                for (i in 0 until numAxes) {
                    val angle = startAngleRad + i * (2 * Math.PI / numAxes)
                    val x = center.x + maxRadius * cos(angle).toFloat()
                    val y = center.y + maxRadius * sin(angle).toFloat()
                    drawLine(
                        color = bentoTextSecondaryColor.copy(alpha = 0.12f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 1.51f.dp.toPx()
                    )

                    drawCircle(
                        color = bentoTextSecondaryColor.copy(alpha = 0.5f),
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                // 3. Draw labels beautifully in deep Bento purple (or light lavender in dark mode)
                val paintColor = bentoTextSecondaryColor.toArgb()
                val paint = android.graphics.Paint().apply {
                    color = paintColor
                    textSize = 10.dp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                for (i in 0 until numAxes) {
                    val angle = startAngleRad + i * (2 * Math.PI / numAxes)
                    val textRadius = maxRadius + 16.dp.toPx()
                    val x = center.x + textRadius * cos(angle).toFloat()
                    val yOffset = if (sin(angle) > 0.1) 12.dp.toPx() else if (sin(angle) < -0.1) -4.dp.toPx() else 4.dp.toPx()
                    val y = center.y + textRadius * sin(angle).toFloat() + yOffset

                    drawContext.canvas.nativeCanvas.drawText(
                        labels[i],
                        x,
                        y,
                        paint
                    )
                }

                // 4. Draw actual data filled polygon
                val dataPath = Path()
                for (i in 0 until numAxes) {
                    val score = animatedValues[i]
                    val factor = (score / 100f).coerceIn(0f, 1f)
                    val r = maxRadius * factor
                    val angle = startAngleRad + i * (2 * Math.PI / numAxes)
                    val x = center.x + r * cos(angle).toFloat()
                    val y = center.y + r * sin(angle).toFloat()

                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                if (numAxes > 0) dataPath.close()

                drawPath(
                    path = dataPath,
                    brush = Brush.radialGradient(
                        colors = listOf(bentoTextSecondaryColor.copy(alpha = 0.35f), bentoTextSecondaryColor.copy(alpha = 0.08f)),
                        center = center,
                        radius = maxRadius
                    )
                )

                // Outlined border match theme
                drawPath(
                    path = dataPath,
                    color = bentoTextSecondaryColor,
                    style = Stroke(width = 2.5f.dp.toPx())
                )

                // Joints
                for (i in 0 until numAxes) {
                    val score = animatedValues[i]
                    val factor = (score / 100f).coerceIn(0f, 1f)
                    val r = maxRadius * factor
                    val angle = startAngleRad + i * (2 * Math.PI / numAxes)
                    val x = center.x + r * cos(angle).toFloat()
                    val y = center.y + r * sin(angle).toFloat()

                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = bentoTextSecondaryColor,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
