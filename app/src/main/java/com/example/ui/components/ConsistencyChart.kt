package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WeeklyLogsCount
import com.example.ui.theme.*

@Composable
fun ConsistencyChart(
    weeklyHistory: List<WeeklyLogsCount>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(BentoSurface)
            .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "WEEKLY ACTIVITY",
            color = BentoTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyHistory.forEach { week ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    var animateHeight by remember { mutableStateOf(false) }
                    LaunchedEffect(week) {
                        animateHeight = true
                    }

                    // Animate fraction from 0f to 1f
                    val scaleFactor by animateFloatAsState(
                        targetValue = if (animateHeight) (week.count / 7f) else 0f,
                        animationSpec = tween(durationMillis = 800),
                        label = "barHeight"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Empty background track in lavender gray
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(BentoSurfaceMuted)
                        )

                        // Filled part in scaled Blues matching bento activity intensity
                        val targetHeightPercent = scaleFactor.coerceIn(0f, 1f)
                        if (targetHeightPercent > 0.02f) {
                            val barColor = when {
                                week.count >= 6 -> BentoGridLevel4
                                week.count >= 4 -> BentoGridLevel3
                                week.count >= 2 -> BentoGridLevel2
                                else -> BentoGridLevel1
                            }
                            
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(targetHeightPercent)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(barColor),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                // Subtle touch indicator dot at the top of the bar
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .padding(top = 2.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color.White.copy(alpha = 0.8f))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${week.count}d",
                        color = BentoTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = week.label,
                        color = BentoTextSecondary.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
