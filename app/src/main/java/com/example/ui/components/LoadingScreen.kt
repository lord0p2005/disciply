package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onFinished: () -> Unit
) {
    var startFadeAndScale by remember { mutableStateOf(false) }

    // Sequence timer: pulse then fade
    LaunchedEffect(Unit) {
        delay(1200) // Fast premium transition
        startFadeAndScale = true
        delay(350)
        onFinished()
    }

    // Interactive scale & fade animations for gorgeous cinematic flow
    val alphaAnim by animateFloatAsState(
        targetValue = if (startFadeAndScale) 0f else 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "fade"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startFadeAndScale) 1.08f else 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "scale"
    )

    // Infinite logo pulsing state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .alpha(alphaAnim),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scaleAnim)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
            ) {
                // outer glow element
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.radialGradient(
                                colors = listOf(BentoTextSecondary.copy(alpha = 0.6f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                        .alpha(pulseGlowAlpha)
                )

                // Main circle frame
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(1.5.dp, BentoTextSecondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        color = BentoTextSecondary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Title
            Text(
                text = "disciply",
                color = BentoTextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-quote
            Text(
                text = "COMMIT TO CONSISTENCY",
                color = BentoTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.75f)
            )
        }
    }
}
