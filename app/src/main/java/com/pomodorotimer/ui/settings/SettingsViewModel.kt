package com.pomodorotimer.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodorotimer.data.preferences.SettingsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val focusDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val longBreakInterval: Int = 4,
    val dailyGoal: Int = 8,
    val autoStart: Boolean = true,
    val whiteNoiseType: Int = 0
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsStore = SettingsStore(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
                SettingsUiState(focus, short, long, interval, goal, auto, noise)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateFocusDuration(minutes: Int) = viewModelScope.launch { settingsStore.setFocusDuration(minutes) }
    fun updateShortBreak(minutes: Int) = viewModelScope.launch { settingsStore.setShortBreakDuration(minutes) }
    fun updateLongBreak(minutes: Int) = viewModelScope.launch { settingsStore.setLongBreakDuration(minutes) }
    fun updateLongBreakInterval(count: Int) = viewModelScope.launch { settingsStore.setLongBreakInterval(count) }
    fun updateDailyGoal(count: Int) = viewModelScope.launch { settingsStore.setDailyGoal(count) }
    fun updateAutoStart(enabled: Boolean) = viewModelScope.launch { settingsStore.setAutoStart(enabled) }
    fun updateWhiteNoise(type: Int) = viewModelScope.launch { settingsStore.setWhiteNoiseType(type) }
}
