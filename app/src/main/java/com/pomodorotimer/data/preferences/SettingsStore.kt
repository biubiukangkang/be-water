package com.pomodorotimer.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    companion object {
        val FOCUS_DURATION = intPreferencesKey("focus_duration")
        val SHORT_BREAK_DURATION = intPreferencesKey("short_break_duration")
        val LONG_BREAK_DURATION = intPreferencesKey("long_break_duration")
        val LONG_BREAK_INTERVAL = intPreferencesKey("long_break_interval")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val AUTO_START = booleanPreferencesKey("auto_start")
        val WHITE_NOISE_TYPE = intPreferencesKey("white_noise_type")

        val DEFAULT_FOCUS = 25
        val DEFAULT_SHORT_BREAK = 5
        val DEFAULT_LONG_BREAK = 15
        val DEFAULT_INTERVAL = 4
        val DEFAULT_DAILY_GOAL = 8
        val DEFAULT_AUTO_START = true
        val DEFAULT_WHITE_NOISE = 0
    }

    val focusDuration: Flow<Int> = context.dataStore.data.map { it[FOCUS_DURATION] ?: DEFAULT_FOCUS }
    val shortBreakDuration: Flow<Int> = context.dataStore.data.map { it[SHORT_BREAK_DURATION] ?: DEFAULT_SHORT_BREAK }
    val longBreakDuration: Flow<Int> = context.dataStore.data.map { it[LONG_BREAK_DURATION] ?: DEFAULT_LONG_BREAK }
    val longBreakInterval: Flow<Int> = context.dataStore.data.map { it[LONG_BREAK_INTERVAL] ?: DEFAULT_INTERVAL }
    val dailyGoal: Flow<Int> = context.dataStore.data.map { it[DAILY_GOAL] ?: DEFAULT_DAILY_GOAL }
    val autoStart: Flow<Boolean> = context.dataStore.data.map { it[AUTO_START] ?: DEFAULT_AUTO_START }
    val whiteNoiseType: Flow<Int> = context.dataStore.data.map { it[WHITE_NOISE_TYPE] ?: DEFAULT_WHITE_NOISE }

    suspend fun setFocusDuration(minutes: Int) {
        context.dataStore.edit { it[FOCUS_DURATION] = minutes }
    }

    suspend fun setShortBreakDuration(minutes: Int) {
        context.dataStore.edit { it[SHORT_BREAK_DURATION] = minutes }
    }

    suspend fun setLongBreakDuration(minutes: Int) {
        context.dataStore.edit { it[LONG_BREAK_DURATION] = minutes }
    }

    suspend fun setLongBreakInterval(count: Int) {
        context.dataStore.edit { it[LONG_BREAK_INTERVAL] = count }
    }

    suspend fun setDailyGoal(count: Int) {
        context.dataStore.edit { it[DAILY_GOAL] = count }
    }

    suspend fun setAutoStart(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_START] = enabled }
    }

    suspend fun setWhiteNoiseType(type: Int) {
        context.dataStore.edit { it[WHITE_NOISE_TYPE] = type }
    }
}
