package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DailyLog
import com.example.ui.theme.*
import com.example.ui.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Heatmap(
    logs: List<DailyLog>,
    onToggleDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate the 154 dates (22 weeks * 7 days)
    val gridDates = remember { DateUtils.getCalendarGridDates(weeksCount = 22) }
    // Map existing logs into a quick-lookup map
    val loggedMap = remember(logs) {
        logs.associateBy { it.date }
    }

    val scrollState = rememberScrollState()

    // Auto-scroll to the end of the heatmap (most recent days) on load
    LaunchedEffect(Unit) {
        delay(300)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(BentoSurface)
            .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DISCIPLINE GRID",
                color = BentoTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Last 5 Months",
                color = BentoTextPrimary.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Weekday labels on the left styled in Muted Lavender
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .height(204.dp), // Align perfectly with cells (7 * 24dp + 6 * 6dp spacing)
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Mon", color = BentoTextSecondary.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "Wed", color = BentoTextSecondary.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "Fri", color = BentoTextSecondary.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "Sun", color = BentoTextSecondary.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Scrollable Grid of 22 Weeks
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                for (col in 0 until 22) {
                    Column(
                        modifier = Modifier
                            .width(24.dp)
                            .height(204.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (row in 0 until 7) {
                            val dayIndex = col * 7 + row
                            val dateStr = gridDates.getOrNull(dayIndex) ?: ""
                            val logEntry = loggedMap[dateStr]
                            val isLogged = logEntry?.isLogged ?: false

                            HeatmapTile(
                                date = dateStr,
                                isLogged = isLogged,
                                onClick = {
                                    if (dateStr.isNotEmpty()) {
                                        onToggleDate(dateStr)
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heatmap Legend in Blues
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                color = BentoTextPrimary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BentoGridEmpty)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BentoGridLevel4)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "More",
                color = BentoTextPrimary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HeatmapTile(
    date: String,
    isLogged: Boolean,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    val gridLevel4Color = BentoGridLevel4

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.82f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tileScale"
    )

    // Dynamic sparks animation when selected
    val particleProgress = remember { Animatable(0f) }

    LaunchedEffect(isLogged) {
        if (isLogged) {
            particleProgress.snapTo(0f)
            particleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = LinearOutSlowInEasing)
            )
        } else {
            particleProgress.snapTo(0f)
        }
    }

    // Map log status and textures
    val tileColor = when {
        isLogged -> {
            // Apply varied grades for historical logs
            val hash = (date.hashCode().let { if (it < 0) -it else it } % 3)
            when (hash) {
                0 -> BentoGridLevel2
                1 -> BentoGridLevel3
                else -> BentoGridLevel4
            }
        }
        DateUtils.getTodayString() == date -> BentoGridLevel1 // Vacant today highlighted soft blue
        else -> BentoGridEmpty
    }

    val borderColor = when {
        DateUtils.getTodayString() == date -> BentoGridLevel4
        isLogged -> Color.Transparent
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(tileColor)
            .border(
                width = if (DateUtils.getTodayString() == date) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        isPressed = true
                        delay(75)
                        isPressed = false
                        onClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Particles burst in Accent Blues
        if (particleProgress.value > 0f && particleProgress.value < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val progression = particleProgress.value
                val particleRadius = size.width * 1.5f * progression
                val sizeOfParticle = 1.8.dp.toPx() * (1f - progression)
                val paintAlpha = 1f - progression

                for (i in 0 until 6) {
                    val angle = Math.toRadians(i * 60.0)
                    val px = center.x + particleRadius * cos(angle).toFloat()
                    val py = center.y + particleRadius * sin(angle).toFloat()

                    drawCircle(
                        color = gridLevel4Color.copy(alpha = paintAlpha),
                        radius = sizeOfParticle,
                        center = Offset(px, py),
                        style = Fill
                    )
                }
            }
        }
    }
}
