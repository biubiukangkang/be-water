package com.pomodorotimer.ui.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pomodorotimer.ui.theme.*
import com.pomodorotimer.ui.timer.components.CircularTimer
import com.pomodorotimer.ui.timer.components.Controls
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

    val timeFontSize = when {
        screenWidthDp < 360 -> 56.sp
        screenWidthDp < 420 -> 72.sp
        else -> 96.sp
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (state.phase) {
                            TimerPhase.FOCUS -> "专注"
                            TimerPhase.SHORT_BREAK -> "短休"
                            TimerPhase.LONG_BREAK -> "长休"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    IconButton(onClick = {
                        showStats = true
                        statsViewModel.refresh()
                    }) {
                        Icon(Icons.Default.BarChart, "统计",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "设置",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
    ) { padding ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    CircularTimer(
                        progress = progress,
                        phaseColor = phaseColor,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.size(220.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeDisplay(seconds = state.remainingSeconds, fontSize = timeFontSize)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    TomatoDots(
                        completedCount = state.completedToday,
                        dailyGoal = state.dailyGoal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "今日 ${state.completedToday}/${state.dailyGoal}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Controls(
                        isRunning = state.isRunning,
                        onStart = { timerViewModel.start() },
                        onPause = { timerViewModel.pause() },
                        onReset = { timerViewModel.reset() }
                    )
                    if (state.phase != TimerPhase.FOCUS && !state.isRunning) {
                        TextButton(onClick = { timerViewModel.skipBreak() }) {
                            Icon(Icons.Default.SkipNext, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("跳过休息")
                        }
                    }
                }
            }
        } else {
            val verticalPadding = when {
                screenHeightDp < 600 -> 16.dp
                screenHeightDp < 800 -> 32.dp
                else -> 64.dp
            }
            val ringSize = when {
                screenHeightDp < 600 -> 180.dp
                screenHeightDp < 800 -> 240.dp
                else -> 280.dp
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
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimeDisplay(seconds = state.remainingSeconds, fontSize = timeFontSize)
                Spacer(modifier = Modifier.height(12.dp))
                TomatoDots(
                    completedCount = state.completedToday,
                    dailyGoal = state.dailyGoal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "今日 ${state.completedToday}/${state.dailyGoal}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Controls(
                    isRunning = state.isRunning,
                    onStart = { timerViewModel.start() },
                    onPause = { timerViewModel.pause() },
                    onReset = { timerViewModel.reset() }
                )
                if (state.phase != TimerPhase.FOCUS && !state.isRunning) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { timerViewModel.skipBreak() }) {
                        Icon(Icons.Default.SkipNext, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("跳过休息")
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
