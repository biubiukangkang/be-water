package com.pomodorotimer.ui.timer

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodorotimer.data.local.AppDatabase
import com.pomodorotimer.data.local.PomodoroRecord
import com.pomodorotimer.data.preferences.SettingsStore
import com.pomodorotimer.service.TimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TimerPhase { FOCUS, SHORT_BREAK, LONG_BREAK }

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.FOCUS,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedToday: Int = 0,
    val currentInSession: Int = 1,
    val dailyGoal: Int = 8,
    val autoStart: Boolean = true,
    val whiteNoiseType: Int = 0,
    val focusDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val longBreakInterval: Int = 4
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.pomodoroDao()
    private val settingsStore = SettingsStore(application)

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null
    private var sessionFocusCount = 0
    private val audioManager = com.pomodorotimer.service.AudioManager(getApplication())

    init {
        viewModelScope.launch {
            combine(
                settingsStore.focusDuration,
                settingsStore.shortBreakDuration,
                settingsStore.longBreakDuration,
                settingsStore.longBreakInterval,
                settingsStore.dailyGoal,
                settingsStore.autoStart,
                settingsStore.whiteNoiseType
            ) { focus, short, long, interval, goal, auto, noise ->
                SettingsSnapshot(focus, short, long, interval, goal, auto, noise)
            }.collect { snapshot ->
                _uiState.update { current ->
                    current.copy(
                        focusDuration = snapshot.focus,
                        shortBreakDuration = snapshot.short,
                        longBreakDuration = snapshot.long,
                        longBreakInterval = snapshot.interval,
                        dailyGoal = snapshot.goal,
                        autoStart = snapshot.autoStart,
                        whiteNoiseType = snapshot.noise
                    )
                }
            }
        }
        refreshDailyCount()
    }

    fun start() {
        if (_uiState.value.isRunning) return
        val state = _uiState.value
        if (state.phase == TimerPhase.FOCUS && state.whiteNoiseType > 0) {
            audioManager.playWhiteNoise(state.whiteNoiseType)
        }
        startService()
        tickJob = viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                updateServiceNotification()
            }
            onPhaseComplete()
        }
    }

    fun pause() {
        tickJob?.cancel()
        stopService()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun reset() {
        tickJob?.cancel()
        stopService()
        audioManager.stopWhiteNoise()
        val state = _uiState.value
        val seconds = when (state.phase) {
            TimerPhase.FOCUS -> state.focusDuration * 60
            TimerPhase.SHORT_BREAK -> state.shortBreakDuration * 60
            TimerPhase.LONG_BREAK -> state.longBreakDuration * 60
        }
        _uiState.update { it.copy(remainingSeconds = seconds, isRunning = false) }
    }

    fun setPhase(phase: TimerPhase) {
        tickJob?.cancel()
        stopService()
        val state = _uiState.value
        val seconds = when (phase) {
            TimerPhase.FOCUS -> state.focusDuration * 60
            TimerPhase.SHORT_BREAK -> state.shortBreakDuration * 60
            TimerPhase.LONG_BREAK -> state.longBreakDuration * 60
        }
        _uiState.update {
            it.copy(phase = phase, remainingSeconds = seconds, isRunning = false)
        }
    }

    fun updateSettings(focus: Int?, short: Int?, long: Int?, interval: Int?, goal: Int?, autoStart: Boolean?, whiteNoise: Int?) {
        viewModelScope.launch {
            focus?.let { settingsStore.setFocusDuration(it) }
            short?.let { settingsStore.setShortBreakDuration(it) }
            long?.let { settingsStore.setLongBreakDuration(it) }
            interval?.let { settingsStore.setLongBreakInterval(it) }
            goal?.let { settingsStore.setDailyGoal(it) }
            autoStart?.let { settingsStore.setAutoStart(it) }
            whiteNoise?.let { settingsStore.setWhiteNoiseType(it) }
        }
    }

    fun skipBreak() {
        val state = _uiState.value
        if (state.phase == TimerPhase.FOCUS) return
        tickJob?.cancel()
        stopService()
        audioManager.stopWhiteNoise()
        if (state.whiteNoiseType > 0) {
            audioManager.playWhiteNoise(state.whiteNoiseType)
        }
        val focusSeconds = state.focusDuration * 60
        _uiState.update {
            it.copy(phase = TimerPhase.FOCUS, remainingSeconds = focusSeconds, isRunning = false)
        }
    }

    // --- Service integration ---

    private fun startService() {
        val context = getApplication<Application>()
        val intent = android.content.Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_REMAINING, _uiState.value.remainingSeconds)
            putExtra(TimerService.EXTRA_PHASE, _uiState.value.phase.name.lowercase())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun updateServiceNotification() {
        val context = getApplication<Application>()
        val intent = android.content.Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_UPDATE
            putExtra(TimerService.EXTRA_REMAINING, _uiState.value.remainingSeconds)
            putExtra(TimerService.EXTRA_PHASE, _uiState.value.phase.name.lowercase())
        }
        context.startService(intent)
    }

    private fun stopService() {
        val context = getApplication<Application>()
        val intent = android.content.Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        context.startService(intent)
    }

    // --- Internal ---

    private fun refreshDailyCount() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayStart = getDayStart(now)
            val dayEnd = dayStart + 24 * 60 * 60 * 1000
            val count = dao.getCompletedCountForDay(dayStart, dayEnd)
            _uiState.update { it.copy(completedToday = count) }
        }
    }

    private suspend fun onPhaseComplete() {
        val state = _uiState.value
        _uiState.update { it.copy(isRunning = false) }
        stopService()

        audioManager.playAlertSound()
        audioManager.vibrate()
        audioManager.stopWhiteNoise()

        if (state.phase == TimerPhase.FOCUS) {
            val record = PomodoroRecord(
                startTime = System.currentTimeMillis() - (state.focusDuration * 60L - state.remainingSeconds) * 1000,
                duration = state.focusDuration * 60,
                type = "focus",
                completed = true
            )
            dao.insert(record)
            sessionFocusCount++
            refreshDailyCount()

            val nextPhase = if (sessionFocusCount >= state.longBreakInterval) {
                sessionFocusCount = 0
                TimerPhase.LONG_BREAK
            } else {
                TimerPhase.SHORT_BREAK
            }
            val nextSeconds = when (nextPhase) {
                TimerPhase.SHORT_BREAK -> state.shortBreakDuration * 60
                TimerPhase.LONG_BREAK -> state.longBreakDuration * 60
                else -> 0
            }
            _uiState.update {
                it.copy(
                    phase = nextPhase,
                    remainingSeconds = nextSeconds,
                    currentInSession = it.currentInSession + 1
                )
            }
        } else {
            val nextSeconds = state.focusDuration * 60
            _uiState.update {
                it.copy(
                    phase = TimerPhase.FOCUS,
                    remainingSeconds = nextSeconds
                )
            }
        }

        if (state.autoStart) {
            if (_uiState.value.phase == TimerPhase.FOCUS && state.whiteNoiseType > 0) {
                audioManager.playWhiteNoise(state.whiteNoiseType)
            }
            start()
        }
    }

    private fun getDayStart(millis: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private data class SettingsSnapshot(
        val focus: Int, val short: Int, val long: Int, val interval: Int,
        val goal: Int, val autoStart: Boolean, val noise: Int
    )
}
