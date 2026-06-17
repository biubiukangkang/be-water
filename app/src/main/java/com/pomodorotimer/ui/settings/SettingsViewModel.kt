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
            settingsStore.focusDuration
                .combine(settingsStore.shortBreakDuration) { a, b -> listOf<Any>(a, b) }
                .combine(settingsStore.longBreakDuration) { l, c -> l + c }
                .combine(settingsStore.longBreakInterval) { l, d -> l + d }
                .combine(settingsStore.dailyGoal) { l, e -> l + e }
                .combine(settingsStore.autoStart) { l, f -> l + f }
                .combine(settingsStore.whiteNoiseType) { l, g -> l + g }
                .collect { v ->
                    _uiState.value = SettingsUiState(
                        focusDuration = v[0] as Int,
                        shortBreakDuration = v[1] as Int,
                        longBreakDuration = v[2] as Int,
                        longBreakInterval = v[3] as Int,
                        dailyGoal = v[4] as Int,
                        autoStart = v[5] as Boolean,
                        whiteNoiseType = v[6] as Int
                    )
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
