package com.pomodorotimer.ui.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pomodorotimer.ui.theme.*
import com.pomodorotimer.ui.timer.components.CircularTimer
import com.pomodorotimer.ui.timer.components.Controls
import com.pomodorotimer.ui.timer.components.GlassCircleButton
import com.pomodorotimer.ui.timer.components.TimeDisplay
import com.pomodorotimer.ui.timer.components.TomatoDots
import com.pomodorotimer.ui.stats.StatsPanel
import com.pomodorotimer.ui.stats.StatsViewModel
import com.pomodorotimer.ui.settings.SettingsPanel
import com.pomodorotimer.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    timerViewModel: TimerViewModel = viewModel(),
    statsViewModel: StatsViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val state by timerViewModel.uiState.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val configuration = LocalConfiguration.current

    var showStats by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val isLandscape = screenWidthDp > screenHeightDp

    val phaseColor by animateColorAsState(
        targetValue = when (state.phase) {
            TimerPhase.FOCUS -> LightAccent
            TimerPhase.SHORT_BREAK -> if (isDarkTheme) DarkSecondary else LightSecondary
            TimerPhase.LONG_BREAK -> if (isDarkTheme) DarkPrimaryVariant else LightPrimaryVariant
        },
        label = "phaseColor"
    )

    val totalSeconds = when (state.phase) {
        TimerPhase.FOCUS -> state.focusDuration * 60
        TimerPhase.SHORT_BREAK -> state.shortBreakDuration * 60
        TimerPhase.LONG_BREAK -> state.longBreakDuration * 60
    }
    val progress = if (totalSeconds > 0) state.remainingSeconds.toFloat() / totalSeconds else 1f

    val phaseLabel = when (state.phase) {
        TimerPhase.FOCUS -> "专注"
        TimerPhase.SHORT_BREAK -> "短休"
        TimerPhase.LONG_BREAK -> "长休"
    }

    val backgroundBrush = remember(isDarkTheme) {
        if (isDarkTheme) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF1E1814),
                    Color(0xFF2A2018),
                    Color(0xFF2A2520),
                    Color(0xFF302520),
                    Color(0xFF2A2218),
                    Color(0xFF25201A)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFE8E0D8),
                    Color(0xFFEDE4D8),
                    Color(0xFFF0E8DC),
                    Color(0xFFEDE4D8),
                    Color(0xFFE8E0D8)
                )
            )
        }
    }

    val backgroundModifier = if (isLandscape) {
        Modifier.background(backgroundBrush)
    } else {
        Modifier.background(MaterialTheme.colorScheme.background)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(backgroundModifier)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "静",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 8.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = phaseLabel,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        GlassCircleButton(
                            onClick = {
                                showStats = true
                                statsViewModel.refresh()
                            },
                            icon = Icons.Default.BarChart,
                            contentDescription = "统计",
                            size = 36.dp,
                            iconSize = 20.dp
                        )
                        GlassCircleButton(
                            onClick = { showSettings = true },
                            icon = Icons.Default.Settings,
                            contentDescription = "设置",
                            size = 36.dp,
                            iconSize = 20.dp
                        )
                    }
                }
            }
        ) { padding ->
            if (isLandscape) {
                val ringSize = 140.dp
                val timeFontSize = 32.sp

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularTimer(
                        progress = progress,
                        phaseColor = phaseColor,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.size(ringSize)
                    ) {
                        TimeDisplay(
                            seconds = state.remainingSeconds,
                            fontSize = timeFontSize
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        TomatoDots(
                            completedCount = state.completedToday,
                            dailyGoal = state.dailyGoal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "今日 ${state.completedToday} / ${state.dailyGoal} 个番茄",
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Controls(
                            isRunning = state.isRunning,
                            onStart = { timerViewModel.start() },
                            onPause = { timerViewModel.pause() },
                            onReset = { timerViewModel.reset() }
                        )
                        if (state.phase != TimerPhase.FOCUS && !state.isRunning) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "跳过休息",
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .clickable { timerViewModel.skipBreak() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            } else {
                val ringSize = when {
                    screenHeightDp < 600 -> 180.dp
                    screenHeightDp < 800 -> 240.dp
                    else -> 280.dp
                }
                val timeFontSize = (ringSize.value * 0.22f).sp
                val verticalPadding = when {
                    screenHeightDp < 600 -> 16.dp
                    screenHeightDp < 800 -> 32.dp
                    else -> 64.dp
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp, vertical = verticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularTimer(
                        progress = progress,
                        phaseColor = phaseColor,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.size(ringSize)
                    ) {
                        TimeDisplay(
                            seconds = state.remainingSeconds,
                            fontSize = timeFontSize
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    TomatoDots(
                        completedCount = state.completedToday,
                        dailyGoal = state.dailyGoal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "今日 ${state.completedToday} / ${state.dailyGoal} 个番茄",
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    Controls(
                        isRunning = state.isRunning,
                        onStart = { timerViewModel.start() },
                        onPause = { timerViewModel.pause() },
                        onReset = { timerViewModel.reset() }
                    )
                    if (state.phase != TimerPhase.FOCUS && !state.isRunning) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "跳过休息",
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .clickable { timerViewModel.skipBreak() }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showStats) {
        ModalBottomSheet(onDismissRequest = { showStats = false }) {
            StatsPanel(viewModel = statsViewModel, onDismiss = { showStats = false })
        }
    }

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
            SettingsPanel(viewModel = settingsViewModel, onDismiss = { showSettings = false })
        }
    }
}
