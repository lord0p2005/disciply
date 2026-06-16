package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ConsistencyChart
import com.example.ui.components.Heatmap
import com.example.ui.components.LoadingScreen
import com.example.ui.components.RadarChart
import com.example.ui.theme.*
import com.example.ui.utils.DateUtils
import com.example.ui.viewmodel.DisciplyStats
import com.example.ui.viewmodel.LogViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkThemeOverride by remember { mutableStateOf<Boolean?>(null) }
            val systemDark = isSystemInDarkTheme()
            val isDark = darkThemeOverride ?: systemDark

            MyApplicationTheme(darkTheme = isDark) {
                var showLoading by remember { mutableStateOf(true) }

                if (showLoading) {
                    LoadingScreen(onFinished = { showLoading = false })
                } else {
                    val logViewModel: LogViewModel = viewModel(
                        factory = LogViewModel.Factory(LocalContext.current)
                    )
                    MainDashboard(
                        viewModel = logViewModel,
                        isDark = isDark,
                        onToggleDarkTheme = {
                            darkThemeOverride = !isDark
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainDashboard(
    viewModel: LogViewModel,
    isDark: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    // Collect states
    val logs by viewModel.allLogs.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Heatmap/Log, 1 = Consistency Stats
    val todayString = DateUtils.getTodayString()
    val isTodayLogged = remember(logs) {
        logs.any { it.date == todayString && it.isLogged }
    }

    var showAdvancedHub by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BentoBackground, // Soft lavender background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = showAdvancedHub,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)) { it } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)) { -it } + fadeOut(tween(300)))
                    } else {
                        (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)) { -it } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)) { it } + fadeOut(tween(300)))
                    }
                },
                label = "hub_screen_transition",
                modifier = Modifier.fillMaxSize()
            ) { isHubOpen ->
                if (isHubOpen) {
                    com.example.ui.components.AdvancedHubScreen(
                        isDark = isDark,
                        onBack = { showAdvancedHub = false }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Main content with slide animations based on tab state
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally(animationSpec = tween(400, easing = EaseInOutCubic)) { it } + fadeIn(tween(400)))
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(400, easing = EaseInOutCubic)) { -it } + fadeOut(tween(400)))
                                } else {
                                    (slideInHorizontally(animationSpec = tween(400, easing = EaseInOutCubic)) { -it } + fadeIn(tween(400)))
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(400, easing = EaseInOutCubic)) { it } + fadeOut(tween(400)))
                                }
                            },
                            label = "tab_transition",
                            modifier = Modifier.fillMaxSize()
                        ) { targetTab ->
                            when (targetTab) {
                                0 -> LogTab(
                                    logs = logs,
                                    stats = stats,
                                    isTodayLogged = isTodayLogged,
                                    isDark = isDark,
                                    onToggleDarkTheme = onToggleDarkTheme,
                                    onToggleToday = { viewModel.toggleLog(todayString) },
                                    onToggleDate = { date -> viewModel.toggleLog(date) },
                                    onOpenHub = { showAdvancedHub = true }
                                )
                                1 -> StatsTab(
                                    stats = stats,
                                    isDark = isDark,
                                    onToggleDarkTheme = onToggleDarkTheme
                                )
                            }
                        }

                        // Custom Floating Navigation Capsule Bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                                .windowInsetsPadding(WindowInsets.navigationBars) // Comply with edge-to-edge guidelines
                        ) {
                            FloatingCapsuleBar(
                                activeTab = activeTab,
                                onTabSelected = { activeTab = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogTab(
    logs: List<com.example.data.db.DailyLog>,
    stats: DisciplyStats,
    isTodayLogged: Boolean,
    isDark: Boolean,
    onToggleDarkTheme: () -> Unit,
    onToggleToday: () -> Unit,
    onToggleDate: (String) -> Unit,
    onOpenHub: () -> Unit
) {
    val scrollState = rememberScrollState()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 100.dp), // Spacious clearance for the floating navigation capsule
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Vibe (Beautifully styled like Bento Grid Title HTML)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "disciply",
                    color = BentoTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "One action a day. Unbroken.",
                    color = BentoTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tactical Rounded Theme toggle key
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BentoBadgeBg)
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onToggleDarkTheme()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDark) "☀️" else "🌙",
                        fontSize = 14.sp
                    )
                }

                var isHubBtnPressed by remember { mutableStateOf(false) }
                val hubBtnScale by animateFloatAsState(
                    targetValue = if (isHubBtnPressed) 0.88f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "hubBtnScale"
                )

                // Current Streak badge as in the bento layout "Current Streak capsule"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .scale(hubBtnScale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BentoBadgeBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    isHubBtnPressed = true
                                    delay(90)
                                    isHubBtnPressed = false
                                    onOpenHub()
                                }
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                // Pulse status indicator dot
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dotPulse"
                )
                
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .background(BentoTextSecondary.copy(alpha = 0.4f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(BentoTextSecondary, CircleShape)
                    )
                }

                Text(
                    text = "HUB ✨",
                    color = BentoBadgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

        // Giant Main Fire Streak Display (Bento Card Layout)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(BentoSurface)
                .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CURRENT STREAK",
                        color = BentoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${stats.currentStreak}",
                            color = BentoTextPrimary,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = if (stats.currentStreak == 1) "DAY" else "DAYS",
                            color = BentoTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .alignByBaseline()
                                .padding(bottom = 12.dp)
                        )
                    }
                }

                // Decorative Circular progress gauge representing streak weight over 30 days
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedPercentage by animateFloatAsState(
                        targetValue = stats.streakScore,
                        animationSpec = tween(durationMillis = 1000),
                        label = "percent"
                    )

                    val surfaceMutedColor = BentoSurfaceMuted
                    val gridLevel4Color = BentoGridLevel4

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background circle
                        drawCircle(
                            color = surfaceMutedColor,
                            radius = size.width / 2f - 4.dp.toPx(),
                            style = Stroke(width = 6.dp.toPx())
                        )
                        // Foreground glowing bento blue circle
                        drawArc(
                            color = gridLevel4Color,
                            startAngle = -90f,
                            sweepAngle = (animatedPercentage / 100f) * 360f,
                            useCenter = false,
                            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                            size = size.copy(
                                width = size.width - 8.dp.toPx(),
                                height = size.height - 8.dp.toPx()
                            ),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${animatedPercentage.toInt()}%",
                            color = BentoTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CONSISTENCY",
                            color = BentoTextSecondary.copy(alpha = 0.7f),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid Stats Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stat 1
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BentoSurface)
                    .border(1.dp, BentoBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "LONGEST STREAK",
                    color = BentoTextSecondary.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stats.longestStreak} Days",
                    color = BentoTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stat 2
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BentoSurface)
                    .border(1.dp, BentoBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "TOTAL COMPLETED",
                    color = BentoTextSecondary.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stats.totalLogsCount} Logs",
                    color = BentoTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heatmap Component
        Heatmap(
            logs = logs,
            onToggleDate = onToggleDate
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap on any grid square to log or undo entries directly.",
            color = BentoTextPrimary.copy(alpha = 0.5f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // BIG TACTILE TODAY TOGGLE ZONE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(if (isTodayLogged) BentoSurfaceMuted else BentoSurface)
                .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isTodayLogged) {
                    Text(
                        text = "TODAY IS VACANT",
                        color = BentoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mark today's target logged.",
                        color = BentoTextPrimary.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Huge pulsing tap action button
                    var tapPressed by remember { mutableStateOf(false) }
                    val buttonScale by animateFloatAsState(
                        targetValue = if (tapPressed) 0.85f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "btnScale"
                    )

                    val btnCoroutine = rememberCoroutineScope()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(54.dp)
                            .scale(buttonScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BentoGridLevel4)
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                btnCoroutine.launch {
                                    tapPressed = true
                                    delay(80)
                                    tapPressed = false
                                    onToggleToday()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOG TODAY",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    // Today is complete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(BentoGridLevel2, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Check",
                                modifier = Modifier.size(18.dp),
                                intentColor = BentoGridLevel4
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "LOGGED FOR TODAY",
                                color = BentoGridLevel4,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Boom. Daily duty complete. 🔥",
                                color = BentoTextPrimary.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tiny Undo Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onToggleToday()
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "UNDO TODAY'S LOG",
                            color = BentoTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// Workaround for Compose standard Icon tint mapping issue
@Composable
fun Icon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    intentColor: Color = LocalContentColor.current
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = intentColor
    )
}

@Composable
fun StatsTab(
    stats: DisciplyStats,
    isDark: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val scrollState = rememberScrollState()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Analytics header vibe
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONSISTENCY MAPS",
                    color = BentoTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Autonomous statistics from logged patterns.",
                    color = BentoTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BentoBadgeBg)
                    .clickable {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggleDarkTheme()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDark) "☀️" else "🌙",
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Radar map
        RadarChart(stats = stats)

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Bar chart past 8 weeks
        ConsistencyChart(weeklyHistory = stats.weeklyHistory)

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Discipline Insights Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Grit insight card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BentoSurface)
                    .border(1.dp, BentoBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "WEEKEND GRIT",
                    color = BentoTextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${stats.gritScore.toInt()}%",
                    color = BentoTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (stats.gritScore >= 80) "Exceptional weekend grit."
                    else if (stats.gritScore >= 50) "Moderate weekend push."
                    else "Weekends need discipline.",
                    color = BentoTextSecondary.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }

            // Focus insight card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BentoSurface)
                    .border(1.dp, BentoBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "WEEKDAY FOCUS",
                    color = BentoTextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${stats.focusScore.toInt()}%",
                    color = BentoTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (stats.focusScore >= 80) "Phenomenal work focus."
                    else if (stats.focusScore >= 50) "Solid work-routine."
                    else "Weekday routines are loose.",
                    color = BentoTextSecondary.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
fun FloatingCapsuleBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    // Elegant pill container floating above components matching Bento theme
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(BentoSurface.copy(alpha = 0.94f)) // Semi-transparent white
            .border(1.dp, BentoBorder, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onTabSelected(0)
                    },
                contentAlignment = Alignment.Center
            ) {
                val isSelected = activeTab == 0
                val transition = updateTransition(targetState = isSelected, label = "tab0")
                
                val scale by transition.animateFloat(label = "scale") { if (it) 1f else 0.88f }
                val alpha by transition.animateFloat(label = "alpha") { if (it) 1f else 0.5f }
                val color by transition.animateColor(label = "color") { if (it) BentoTextSecondary else BentoTextPrimary.copy(alpha = 0.6f) }
                val backgroundIndicatorColor by transition.animateColor(label = "bgIndicator") { if (it) BentoBadgeBg else Color.Transparent }

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(18.dp))
                        .background(backgroundIndicatorColor)
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val sqWidth = 6.dp.toPx()
                            val sqHeight = 6.dp.toPx()
                            val space = 2.dp.toPx()
                            for (col in 0 until 2) {
                                for (row in 0 until 2) {
                                    val dx = col * (sqWidth + space)
                                    val dy = row * (sqHeight + space)
                                    drawRect(
                                        color = color,
                                        topLeft = Offset(dx, dy),
                                        size = size.copy(width = sqWidth, height = sqHeight)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "LOG",
                            color = color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Tab 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onTabSelected(1)
                    },
                contentAlignment = Alignment.Center
            ) {
                val isSelected = activeTab == 1
                val transition = updateTransition(targetState = isSelected, label = "tab1")
                
                val scale by transition.animateFloat(label = "scale") { if (it) 1f else 0.88f }
                val alpha by transition.animateFloat(label = "alpha") { if (it) 1f else 0.5f }
                val color by transition.animateColor(label = "color") { if (it) BentoTextSecondary else BentoTextPrimary.copy(alpha = 0.6f) }
                val backgroundIndicatorColor by transition.animateColor(label = "bgIndicator") { if (it) BentoBadgeBg else Color.Transparent }

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(18.dp))
                        .background(backgroundIndicatorColor)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val strokeWidth = 1.5.dp.toPx()
                            drawCircle(
                                color = color,
                                radius = size.width / 2,
                                style = Stroke(width = strokeWidth)
                             )
                            drawCircle(
                                color = color,
                                radius = size.width / 4,
                                style = Stroke(width = strokeWidth)
                            )
                            drawCircle(
                                color = color,
                                radius = 2.dp.toPx(),
                                style = Fill
                            )
                        }
                        Text(
                            text = "INSIGHTS",
                            color = color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
