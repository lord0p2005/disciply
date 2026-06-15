package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WeeklyLogsCount
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConsistencyChart(
    weeklyHistory: List<WeeklyLogsCount>,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedWeek by remember { mutableStateOf<WeeklyLogsCount?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(BentoSurface)
            .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        // Top row with Title and custom feedback chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEEKLY ACTIVITY",
                color = BentoTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            if (selectedWeek != null) {
                val percent = (selectedWeek!!.count * 100 / 7)
                Text(
                    text = "${selectedWeek!!.label}: ${selectedWeek!!.count}/7 (${percent}%)",
                    color = BentoGridLevel4,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BentoGridLevel4.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            } else {
                Text(
                    text = "Tap bars for metrics",
                    color = BentoTextPrimary.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyHistory.forEach { week ->
                val isCurrentSelection = selectedWeek == week
                val coroutineScope = rememberCoroutineScope()
                var isPressed by remember { mutableStateOf(false) }

                // Elastic scale on user tap
                val elementScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else if (isCurrentSelection) 1.05f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "barScale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .scale(elementScale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedWeek = if (selectedWeek == week) null else week
                                coroutineScope.launch {
                                    isPressed = true
                                    delay(90)
                                    isPressed = false
                                }
                            }
                        )
                ) {
                    var animateHeight by remember { mutableStateOf(false) }
                    LaunchedEffect(week) {
                        animateHeight = true
                    }

                    // Animate fraction with smooth bouncy elastic expansion
                    val scaleFactor by animateFloatAsState(
                        targetValue = if (animateHeight) (week.count / 7f) else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "barHeight"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Empty background track
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrentSelection) BentoSurfaceMuted.copy(alpha = 1.3f) else BentoSurfaceMuted)
                        )

                        // Filled part
                        val targetHeightPercent = scaleFactor.coerceIn(0f, 1f)
                        if (targetHeightPercent > 0.02f) {
                            val barColor = when {
                                week.count >= 6 -> BentoGridLevel4
                                week.count >= 4 -> BentoGridLevel3
                                week.count >= 2 -> BentoGridLevel2
                                else -> BentoGridLevel3.copy(alpha = 0.7f) // vibrant active minimum
                            }
                            
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(targetHeightPercent)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(barColor),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                // Dynamic white accent dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .padding(top = 2.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color.White.copy(alpha = 0.9f))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${week.count}d",
                        color = if (isCurrentSelection) BentoGridLevel4 else BentoTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = week.label,
                        color = if (isCurrentSelection) BentoGridLevel4.copy(alpha = 0.8f) else BentoTextSecondary.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
