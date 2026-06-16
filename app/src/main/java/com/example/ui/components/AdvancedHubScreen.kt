package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdvancedHubScreen(
    isDark: Boolean,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Bouncy press animation for Back Button
    var isBackBtnPressed by remember { mutableStateOf(false) }
    val backBtnScale by animateFloatAsState(
        targetValue = if (isBackBtnPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "backBtnScale"
    )

    // Sensor State Machine
    var isSyncing by remember { mutableStateOf(false) }
    var syncCompleted by remember { mutableStateOf(false) }
    var stepsCount by remember { mutableStateOf(0) }
    var screenMinutes by remember { mutableStateOf(0) }
    var sleepHoursValue by remember { mutableStateOf(0.0) }
    var activeMinutes by remember { mutableStateOf(0) }
    var liveFeedbackText by remember { mutableStateOf("Ready to ingest local device insights. Push sync below.") }

    // Widget Sandbox state
    var selectedWidgetTheme by remember { mutableStateOf(0) } // 0 = Obsidian, 1 = Sapphire, 2 = Emerald
    var showWidgetFlame by remember { mutableStateOf(true) }
    var showWidgetStreak by remember { mutableStateOf(true) }

    // Active sub-section
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Lab Modules, 1 = Tech Architecture Proposal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HUB APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular back chevron with satisfying custom bouncy gesture
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(backBtnScale)
                        .clip(CircleShape)
                        .background(BentoBadgeBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    isBackBtnPressed = true
                                    delay(90)
                                    isBackBtnPressed = false
                                    onBack()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        color = BentoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "disciply lab",
                        color = BentoTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Next-Gen Sensor Sync & Widgets Studio",
                        color = BentoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Tech Badge indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(BentoGridLevel4.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "EXPERIMENTAL v2.1",
                    color = BentoGridLevel4,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DOUBLE PILL SWITCHER FOR MODULES VS TECHNICAL DOCUMENTATION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BentoSurface)
                .border(1.dp, BentoBorder, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("STUDIO SANDBOX", "INTEGRATION PROPOSAL").forEachIndexed { index, label ->
                val isSelected = activeSubTab == index
                val subTabScale = remember { Animatable(1f) }
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(subTabScale.value)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) BentoBadgeBg else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    subTabScale.animateTo(0.92f, spring())
                                    activeSubTab = index
                                    subTabScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                                }
                            }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) BentoTextPrimary else BentoTextSecondary.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (activeSubTab == 0) {
            // ================== SANDBOX TAB ==================
            
            // CARD 1: DEVICE HEALTH SENSORS & SCREEN TIME ENGINE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(BentoSurface)
                    .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DEVICE TELEMETRY RIG",
                                color = BentoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Health, Sleep & Screen Analytics",
                                color = BentoTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status light
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (syncCompleted) BentoGridLevel4 else Color.Red.copy(alpha = 0.6f))
                            )
                            Text(
                                text = if (syncCompleted) "SYNCHRONIZED" else "STALE",
                                color = if (syncCompleted) BentoGridLevel4 else BentoTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated metrics columns with beautiful progress sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Metric Column 1: Steps (Physical)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BentoBackground)
                                .padding(12.dp)
                        ) {
                            Text(text = "👟 STEPS", color = BentoTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (syncCompleted) "$stepsCount" else "--",
                                color = BentoTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Simple visual progress bar representing step density
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(BentoSurfaceMuted)
                            ) {
                                val animatedFillWidth by animateFloatAsState(
                                    targetValue = if (syncCompleted) (stepsCount / 10000f).coerceIn(0f, 1f) else 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessVeryLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedFillWidth)
                                        .background(BentoGridLevel3)
                                )
                            }
                        }

                        // Metric Column 2: Screen Time (Digital Core)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BentoBackground)
                                .padding(12.dp)
                        ) {
                            Text(text = "📱 SCREEN", color = BentoTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (syncCompleted) "${screenMinutes}m" else "--",
                                color = BentoTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(BentoSurfaceMuted)
                            ) {
                                val animatedFillWidth by animateFloatAsState(
                                    targetValue = if (syncCompleted) (1f - (screenMinutes / 480f).coerceIn(0f, 1f)) else 0f, // Less is better!
                                    animationSpec = spring(stiffness = Spring.StiffnessVeryLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedFillWidth)
                                        .background(BentoGridLevel4)
                                )
                            }
                        }

                        // Metric Column 3: Sleep & Bio
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BentoBackground)
                                .padding(12.dp)
                        ) {
                            Text(text = "🌙 SLEEP", color = BentoTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (syncCompleted) "${sleepHoursValue}h" else "--",
                                color = BentoTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(BentoSurfaceMuted)
                            ) {
                                val animatedFillWidth by animateFloatAsState(
                                    targetValue = if (syncCompleted) (sleepHoursValue / 9.0).coerceIn(0.0, 1.0).toFloat() else 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessVeryLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedFillWidth)
                                        .background(BentoGridLevel2)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // DYNAMIC INSIGHT LAB FEEDBACK BOARD (Animated speech bubble)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(BentoBackground)
                            .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "🧠",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    text = "DISCIPLY COGNITIVE AI FEEDBACK",
                                    color = BentoGridLevel4,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                AnimatedContent(
                                    targetState = liveFeedbackText,
                                    transitionSpec = {
                                        (fadeIn(tween(300)) + slideInVertically { it / 2 })
                                            .togetherWith(fadeOut(tween(250)) + slideOutVertically { -it / 2 })
                                    },
                                    label = "insightTextAnim"
                                ) { text ->
                                    Text(
                                        text = text,
                                        color = BentoTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // SYNC TRIGGERS (Highly interactive, bouncy action button)
                    var isSyncBtnPressed by remember { mutableStateOf(false) }
                    val syncBtnScale by animateFloatAsState(
                        targetValue = if (isSyncBtnPressed || isSyncing) 0.92f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
                        label = "syncBtn"
                    )

                    val syncButtonBrush = if (syncCompleted) {
                        Brush.linearGradient(colors = listOf(BentoBadgeBg, BentoBadgeBg))
                    } else {
                        Brush.horizontalGradient(colors = listOf(BentoGridLevel4, BentoGridLevel3))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(syncBtnScale)
                            .clip(RoundedCornerShape(20.dp))
                            .background(syncButtonBrush)
                            .clickable(
                                enabled = !isSyncing,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        isSyncBtnPressed = true
                                        isSyncing = true
                                        liveFeedbackText = "Binding Health Connect API framework & scanning active sensors..."
                                        delay(1200)
                                        
                                        liveFeedbackText = "Extracting daily screen usage indices from UsageStatsService..."
                                        stepsCount = (6000..10500).random()
                                        screenMinutes = (120..290).random()
                                        sleepHoursValue = (60 + (0..25).random()) / 10.0
                                        activeMinutes = (15..75).random()
                                        
                                        isSyncBtnPressed = false
                                        delay(1200)
                                        
                                        liveFeedbackText = "Ingestion complete. Analyzing correlation: Steps offset: ${stepsCount - 7000} vs target, Screen reduction: ${300 - screenMinutes} mins. Core momentum boosted!"
                                        syncCompleted = true
                                        isSyncing = false
                                    }
                                }
                            )
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "SCANNING TELEMETRY...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            } else {
                                Text(
                                    text = if (syncCompleted) "🔄 TELEMETRY RE-SYNC" else "⚡ INGEST HEALTH & SCREEN SENSORS",
                                    color = if (syncCompleted) BentoTextPrimary else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD 2: APP WIDGETS LABORATORY & LIVE PREVIEWS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(BentoSurface)
                    .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "HOME SCREEN WORKSHOP",
                        color = BentoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Glance API Widget Live Mockups",
                        color = BentoTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch modifiers for styling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aesthetic Themes:",
                            color = BentoTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        listOf("Obsidian", "Sapphire", "Emerald").forEachIndexed { valIndex, themeName ->
                            val isThemeSel = selectedWidgetTheme == valIndex
                            val themeBtnScale = remember { Animatable(1f) }
                            val scope = rememberCoroutineScope()

                            Box(
                                modifier = Modifier
                                    .scale(themeBtnScale.value)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isThemeSel) BentoBadgeBg else BentoBackground)
                                    .border(1.dp, if (isThemeSel) BentoBorder else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        scope.launch {
                                            themeBtnScale.animateTo(0.85f, spring())
                                            selectedWidgetTheme = valIndex
                                            themeBtnScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = themeName,
                                    color = if (isThemeSel) BentoTextPrimary else BentoTextSecondary.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // THE DYNAMIC LIVE WIDGET PREVIEW BOARD
                    Text(
                        text = "LIVE WIDGET PREVIEW (2x2 Compact Grid)",
                        color = BentoTextSecondary.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Widget Box representing home screen Glance card
                    val widgetBgColor = when (selectedWidgetTheme) {
                        0 -> Color(0xFF13111C)
                        1 -> Color(0xFF1D263B)
                        else -> Color(0xFF13221C)
                    }
                    val widgetAccentColor = when (selectedWidgetTheme) {
                        0 -> BentoGridLevel4
                        1 -> Color(0xFF74A2E7)
                        else -> Color(0xFF68C991)
                    }

                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(24.dp))
                            .background(widgetBgColor)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "disciply",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        text = "WIDGET",
                                        color = widgetAccentColor,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Text(
                                    text = "🔥",
                                    fontSize = 18.sp
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "3",
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Light,
                                        modifier = Modifier.alignByBaseline()
                                    )
                                    Text(
                                        text = "DAYS",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier
                                            .alignByBaseline()
                                            .padding(bottom = 4.dp)
                                        
                                    )
                                }

                                Text(
                                    text = if (syncCompleted) "Synced $stepsCount Steps" else "No device sync done",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 10.sp
                                )
                            }

                            // Tiny mini progress indicator bars inside widget representing activity consistency map
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(5) { barIndex ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (barIndex < 3) widgetAccentColor else Color.White.copy(
                                                    alpha = 0.1f
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "💡 Tap different aesthetic themes above to see widgets shift visual palette. Glance API utilizes local state repositories in Room db to update real Android Home Screen cells efficiently.",
                        color = BentoTextPrimary.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

        } else {
            // ================== ARCHITECTURE PROPOSAL TAB ==================
            
            // INTRODUCING PROPOSAL METRICS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(BentoSurface)
                    .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SECURE SYNC STRATEGY",
                        color = BentoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Google Health Connect & Jetpack Glance",
                        color = BentoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "To bridge native mobile parameters directly into Disciply's state system securely, the optimal, production-ready framework operates over three key layers:",
                        color = BentoTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CORE ARCHITECTURE BLOCK 1: HEALTH CONNECT
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BentoBackground)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "1. Wearables & Health Connect Integration",
                            color = BentoGridLevel4,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Schema Registration: Integrate standard Google Health Connect SDK with granular permissions (StepsRecord, SleepSessionRecord, StepsCadenceRecord).\n" +
                                   "• Local Handshake: Disciply requests specific read permissions. Data resides purely in Google's encrypted local hardware safe on-device, absolute zero cloudy leakage.\n" +
                                   "• Periodic Queries: Execute bounded retro-queries during app launch or WorkManager routines to update logs.",
                            color = BentoTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CORE ARCHITECTURE BLOCK 2: SCREEN TIME
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BentoBackground)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "2. Focus Core: Screen Usage & Digital Vitals",
                            color = BentoGridLevel3,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• API: Declare android.permission.PACKAGE_USAGE_STATS.\n" +
                                   "• Metrics: Measure aggregated foreground durations using UsageStatsManager. Determine user distraction indexes vs target focus regimes.\n" +
                                   "• Local Feedbacks: Translate high screen index to dynamic habit weightings and show supportive AI feedback within milliseconds.",
                            color = BentoTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CORE ARCHITECTURE BLOCK 3: PERSISTENCE & WIDGETS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BentoBackground)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "3. Jetpack Glance Home Screen Widgets",
                            color = BentoGridLevel2,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Glance API: Write GlanceAppWidget utilizing Jetpack Compose DSL for highly responsive layout compositions.\n" +
                                   "• State Sync: Subscribe widget structures directly to database changes, refreshing layout whenever a habit gets flagged.\n" +
                                   "• Intent Bindings: Tap widgets to trigger quick-log actions instantly without launching fullscreen elements.",
                            color = BentoTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
