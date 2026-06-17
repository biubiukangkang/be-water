# 「静」番茄钟 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a production-ready Android Pomodoro timer app with wabi-sabi aesthetic, adaptive layout, and background timer service.

**Architecture:** Single-Activity + Jetpack Compose UI layer driving via ViewModel StateFlows. Room for persistent statistics, DataStore for preferences, Foreground Service for background timing.

**Tech Stack:** Kotlin, Jetpack Compose, Room, DataStore, Foreground Service, MediaPlayer

---

### Task 1: Project Scaffolding

**Files:**
- Create: `静/build.gradle.kts`
- Create: `静/settings.gradle.kts`
- Create: `静/gradle.properties`
- Create: `静/app/build.gradle.kts`
- Create: `静/app/src/main/AndroidManifest.xml`
- Create: `静/app/src/main/res/values/strings.xml`
- Create: `静/.gitignore`
- Create: `静/gradle/wrapper/gradle-wrapper.properties`

- [ ] **Step 1: Create root build.gradle.kts**

```kotlin
// 静/build.gradle.kts
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
```

- [ ] **Step 2: Create settings.gradle.kts**

```kotlin
// 静/settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "jing"
include(":app")
```

- [ ] **Step 3: Create gradle.properties**

```properties
# 静/gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: Create app/build.gradle.kts**

```kotlin
// 静/app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.pomodorotimer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pomodorotimer.jing"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

- [ ] **Step 5: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:name=".PomodoroApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.TimerService"
            android:foregroundServiceType="specialUse"
            android:exported="false" />
    </application>
</manifest>
```

- [ ] **Step 6: Create strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">静</string>
</resources>
```

- [ ] **Step 7: Create .gitignore**

```
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
```

- [ ] **Step 8: Create gradle-wrapper.properties**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

---

### Task 2: Application & Theme Layer

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/PomodoroApp.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/ui/theme/Color.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/ui/theme/Type.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/ui/theme/Theme.kt`

- [ ] **Step 1: Create PomodoroApp.kt**

```kotlin
package com.pomodorotimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class PomodoroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val timerChannel = NotificationChannel(
            CHANNEL_TIMER,
            "计时器",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示剩余专注时间"
            setSound(null, null)
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERT,
            "提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "专注/休息结束提醒"
            enableVibration(true)
        }

        manager.createNotificationChannel(timerChannel)
        manager.createNotificationChannel(alertChannel)
    }

    companion object {
        const val CHANNEL_TIMER = "timer"
        const val CHANNEL_ALERT = "alert"
    }
}
```

- [ ] **Step 2: Create Color.kt**

```kotlin
package com.pomodorotimer.ui.theme

import androidx.compose.ui.graphics.Color

// Light mode - 侘寂风
val LightBackground = Color(0xFFF5F0EB)
val LightSurface = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF3D3530)
val LightOnSurface = Color(0xFF3D3530)
val LightSecondary = Color(0xFF8B7E74)
val LightPrimary = Color(0xFFD4C5B5)
val LightPrimaryVariant = Color(0xFFB8A898)
val LightAccent = Color(0xFFC4A882)
val LightCard = Color(0xFFFFFFFF)

// Dark mode
val DarkBackground = Color(0xFF2A2520)
val DarkSurface = Color(0xFF35302A)
val DarkOnBackground = Color(0xFFE8E0D8)
val DarkOnSurface = Color(0xFFE8E0D8)
val DarkSecondary = Color(0xFFA09888)
val DarkPrimary = Color(0xFFA09080)
val DarkPrimaryVariant = Color(0xFFC4B0A0)
val DarkAccent = Color(0xFFB8A088)
val DarkCard = Color(0xFF35302A)

// Progress colors
val ProgressComplete = Color(0xFF8B7355)
val ProgressEmpty = Color(0xFFE0D8D0)
val ProgressEmptyDark = Color(0xFF4A4540)
```

- [ ] **Step 3: Create Type.kt**

```kotlin
package com.pomodorotimer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 96.sp,
        letterSpacing = 4.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 64.sp,
        letterSpacing = 2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)
```

- [ ] **Step 4: Create Theme.kt**

```kotlin
package com.pomodorotimer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnBackground,
    primaryContainer = LightPrimaryVariant,
    secondary = LightSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightCard,
    outline = LightSecondary.copy(alpha = 0.3f)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnBackground,
    primaryContainer = DarkPrimaryVariant,
    secondary = DarkSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkCard,
    outline = DarkSecondary.copy(alpha = 0.3f)
)

@Composable
fun JingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

### Task 3: Data Layer — Room Database

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/data/local/PomodoroRecord.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/data/local/PomodoroDao.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/data/local/AppDatabase.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/data/preferences/SettingsStore.kt`

- [ ] **Step 1: Create PomodoroRecord.kt**

```kotlin
package com.pomodorotimer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_records")
data class PomodoroRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,       // epoch millis
    val duration: Int,         // seconds
    val type: String,          // "focus", "short_break", "long_break"
    val completed: Boolean     // true if finished without cancel
)
```

- [ ] **Step 2: Create PomodoroDao.kt**

```kotlin
package com.pomodorotimer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Insert
    suspend fun insert(record: PomodoroRecord)

    @Query("SELECT * FROM pomodoro_records WHERE startTime >= :dayStart AND startTime < :dayEnd ORDER BY startTime DESC")
    suspend fun getRecordsForDay(dayStart: Long, dayEnd: Long): List<PomodoroRecord>

    @Query("SELECT * FROM pomodoro_records WHERE startTime >= :weekStart ORDER BY startTime DESC")
    fun getRecordsSince(weekStart: Long): Flow<List<PomodoroRecord>>

    @Query("SELECT COUNT(*) FROM pomodoro_records WHERE type = 'focus' AND completed = 1 AND startTime >= :dayStart AND startTime < :dayEnd")
    suspend fun getCompletedCountForDay(dayStart: Long, dayEnd: Long): Int

    @Query("SELECT * FROM pomodoro_records WHERE startTime >= :monthStart ORDER BY startTime ASC")
    suspend fun getRecordsSinceTimestamp(monthStart: Long): List<PomodoroRecord>
}
```

- [ ] **Step 3: Create AppDatabase.kt**

```kotlin
package com.pomodorotimer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PomodoroRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jing_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

- [ ] **Step 4: Create SettingsStore.kt**

```kotlin
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
        val DEFAULT_WHITE_NOISE = 0  // 0 = off, 1 = rain, 2 = ocean, 3 = campfire, 4 = white
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
```

---

### Task 4: Timer Core — ViewModel

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/ui/timer/TimerViewModel.kt`

- [ ] **Step 1: Create TimerViewModel.kt**

```kotlin
package com.pomodorotimer.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodorotimer.data.local.AppDatabase
import com.pomodorotimer.data.local.PomodoroRecord
import com.pomodorotimer.data.preferences.SettingsStore
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
    val currentInSession: Int = 1,   // 1-based, within current cycle
    val dailyGoal: Int = 8,
    val autoStart: Boolean = true,
    val whiteNoiseType: Int = 0,
    // settings display values
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
    private var sessionFocusCount = 0  // number of focuses completed in this cycle

    init {
        viewModelScope.launch {
            // Load preferences
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
                val current = _uiState.value
                _uiState.value = current.copy(
                    focusDuration = snapshot.focus,
                    shortBreakDuration = snapshot.shortBreak,
                    longBreakDuration = snapshot.longBreak,
                    longBreakInterval = snapshot.interval,
                    dailyGoal = snapshot.goal,
                    autoStart = snapshot.autoStart,
                    whiteNoiseType = snapshot.noise
                )
            }
        }

        refreshDailyCount()
    }

    private fun refreshDailyCount() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayStart = getDayStart(now)
            val dayEnd = dayStart + 24 * 60 * 60 * 1000
            val count = dao.getCompletedCountForDay(dayStart, dayEnd)
            _uiState.update { it.copy(completedToday = count) }
        }
    }

    fun start() {
        if (_uiState.value.isRunning) return
        tickJob = viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            onPhaseComplete()
        }
    }

    fun pause() {
        tickJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun reset() {
        tickJob?.cancel()
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

    private suspend fun onPhaseComplete() {
        val state = _uiState.value
        _uiState.update { it.copy(isRunning = false) }

        if (state.phase == TimerPhase.FOCUS) {
            // Record completed focus
            val record = PomodoroRecord(
                startTime = System.currentTimeMillis() - (state.focusDuration * 60L - state.remainingSeconds) * 1000,
                duration = state.focusDuration * 60,
                type = "focus",
                completed = true
            )
            dao.insert(record)
            sessionFocusCount++
            refreshDailyCount()

            // Determine next phase
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
            // Break complete — auto-start next focus
            val nextSeconds = state.focusDuration * 60
            _uiState.update {
                it.copy(
                    phase = TimerPhase.FOCUS,
                    remainingSeconds = nextSeconds
                )
            }
        }

        // Auto-start next phase if enabled
        if (state.autoStart) {
            start()
        }
    }

    fun skipBreak() {
        val state = _uiState.value
        if (state.phase == TimerPhase.FOCUS) return  // can't skip focus
        tickJob?.cancel()
        val focusSeconds = state.focusDuration * 60
        _uiState.update {
            it.copy(phase = TimerPhase.FOCUS, remainingSeconds = focusSeconds, isRunning = false)
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
```

---

### Task 5: Timer UI Components

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/ui/timer/components/CircularTimer.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/ui/timer/components/TimeDisplay.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/ui/timer/components/Controls.kt`
- Create: `静/app/src/main/java/com/pomodorotimer/ui/timer/components/TomatoDots.kt`

- [ ] **Step 1: Create CircularTimer.kt**

```kotlin
package com.pomodorotimer.ui.timer.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.pomodorotimer.ui.theme.*

@Composable
fun CircularTimer(
    progress: Float,          // 0f..1f
    phaseColor: Color,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxSize().padding(12.dp)) {
        val strokeWidth = 6.dp.toPx()
        val padding = strokeWidth / 2
        val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
        val topLeft = Offset(padding, padding)

        // Background ring
        drawArc(
            color = if (isDarkTheme) ProgressEmptyDark else ProgressEmpty,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress ring
        drawArc(
            color = phaseColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
```

- [ ] **Step 2: Create TimeDisplay.kt**

```kotlin
package com.pomodorotimer.ui.timer.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TimeDisplay(
    seconds: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 96.sp
) {
    val minutes = seconds / 60
    val secs = seconds % 60
    val text = "%02d:%02d".format(minutes, secs)

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Light,
        letterSpacing = 4.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}
```

- [ ] **Step 3: Create Controls.kt**

```kotlin
package com.pomodorotimer.ui.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Controls(
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset button
        FilledTonalButton(
            onClick = onReset,
            shape = CircleShape,
            modifier = Modifier.size(56.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "重置",
                modifier = Modifier.size(24.dp)
            )
        }

        // Start/Pause button
        Button(
            onClick = { if (isRunning) onPause() else onStart() },
            shape = CircleShape,
            modifier = Modifier.size(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "暂停" else "开始",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
```

- [ ] **Step 4: Create TomatoDots.kt**

```kotlin
package com.pomodorotimer.ui.timer.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pomodorotimer.ui.theme.ProgressComplete
import com.pomodorotimer.ui.theme.ProgressEmpty

@Composable
fun TomatoDots(
    completedCount: Int,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayCount = dailyGoal.coerceIn(1, 12)
        for (i in 0 until displayCount) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = if (i < completedCount) ProgressComplete
                else if (MaterialTheme.colorScheme.background == com.pomodorotimer.ui.theme.DarkBackground)
                    com.pomodorotimer.ui.theme.ProgressEmptyDark
                else ProgressEmpty
            ) {}
        }
    }
}
```

---

### Task 6: Stats ViewModel

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/ui/stats/StatsViewModel.kt`

- [ ] **Step 1: Create StatsViewModel.kt**

```kotlin
package com.pomodorotimer.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodorotimer.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class StatsUiState(
    val todayCount: Int = 0,
    val weekData: List<DailyStats> = emptyList(),
    val monthData: List<DailyStats> = emptyList(),
    val totalFocusHours: Float = 0f,
    val currentStreak: Int = 0
)

data class DailyStats(
    val dayLabel: String,   // "周一", "6/15" etc
    val count: Int
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).pomodoroDao()

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            // Today
            cal.timeInMillis = now
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val todayCount = dao.getCompletedCountForDay(dayStart, dayStart + 86400000L)

            // Week
            cal.add(Calendar.DAY_OF_YEAR, -(Calendar.DAY_OF_WEEK - 1))
            val weekStart = cal.timeInMillis
            val weekRecords = dao.getRecordsSinceTimestamp(weekStart)
            val weekMap = mutableMapOf<Int, Int>()
            val dayNames = arrayOf("日", "一", "二", "三", "四", "五", "六")
            for (r in weekRecords) {
                if (r.type == "focus" && r.completed) {
                    val dayCal = Calendar.getInstance().apply { timeInMillis = r.startTime }
                    val dayIdx = dayCal.get(Calendar.DAY_OF_WEEK) - 1
                    weekMap[dayIdx] = (weekMap[dayIdx] ?: 0) + 1
                }
            }
            val weekData = (0..6).map { idx ->
                DailyStats(dayNames[idx], weekMap[idx] ?: 0)
            }

            // Month
            cal.timeInMillis = now
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis
            val monthRecords = dao.getRecordsSinceTimestamp(monthStart)
            val monthMap = mutableMapOf<Int, Int>()
            var totalFocusSec = 0
            for (r in monthRecords) {
                if (r.type == "focus" && r.completed) {
                    val dayCal = Calendar.getInstance().apply { timeInMillis = r.startTime }
                    val day = dayCal.get(Calendar.DAY_OF_MONTH)
                    monthMap[day] = (monthMap[day] ?: 0) + 1
                    totalFocusSec += r.duration
                }
            }
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthData = (1..daysInMonth).map { day ->
                DailyStats("${day}", monthMap[day] ?: 0)
            }

            _uiState.value = StatsUiState(
                todayCount = todayCount,
                weekData = weekData,
                monthData = monthData,
                totalFocusHours = totalFocusSec / 3600f
            )
        }
    }
}
```

---

### Task 7: Settings ViewModel

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/ui/settings/SettingsViewModel.kt`

- [ ] **Step 1: Create SettingsViewModel.kt**

```kotlin
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
```

---

### Task 8: Audio Manager

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/service/AudioManager.kt`

- [ ] **Step 1: Create AudioManager.kt**

```kotlin
package com.pomodorotimer.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build

class AudioManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playWhiteNoise(type: Int) {
        if (type <= 0) return
        stopWhiteNoise()

        val rawResId = when (type) {
            1 -> context.resources.getIdentifier("rain", "raw", context.packageName)
            2 -> context.resources.getIdentifier("ocean", "raw", context.packageName)
            3 -> context.resources.getIdentifier("campfire", "raw", context.packageName)
            4 -> context.resources.getIdentifier("whitenoise", "raw", context.packageName)
            else -> 0
        }

        if (rawResId == 0) return

        val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(context, uri)
            isLooping = true
            setVolume(0.5f, 0.5f)
            prepare()
            start()
        }
    }

    fun stopWhiteNoise() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    fun playAlertSound() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (notification != null) {
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        }
    }

    fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun release() {
        stopWhiteNoise()
    }
}
```

---

### Task 9: Timer Service (Foreground Service)

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/service/TimerService.kt`

- [ ] **Step 1: Create TimerService.kt**

```kotlin
package com.pomodorotimer.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pomodorotimer.MainActivity
import com.pomodorotimer.PomodoroApp
import com.pomodorotimer.R

class TimerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val remainingSeconds = intent.getIntExtra(EXTRA_REMAINING, 0)
                val phase = intent.getStringExtra(EXTRA_PHASE) ?: "focus"
                startForeground(NOTIFICATION_ID, createTimerNotification(remainingSeconds, phase))
            }
            ACTION_UPDATE -> {
                val remainingSeconds = intent.getIntExtra(EXTRA_REMAINING, 0)
                val phase = intent.getStringExtra(EXTRA_PHASE) ?: "focus"
                val notification = createTimerNotification(remainingSeconds, phase)
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createTimerNotification(remainingSeconds: Int, phase: String): android.app.Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = "%02d:%02d".format(minutes, seconds)
        val phaseText = if (phase == "focus") "专注中" else "休息中"

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, PomodoroApp.CHANNEL_TIMER)
            .setContentTitle("静 · $phaseText")
            .setContentText("剩余 $timeText")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_TIMER = "com.pomodorotimer.action.START_TIMER"
        const val ACTION_UPDATE = "com.pomodorotimer.action.UPDATE"
        const val ACTION_STOP = "com.pomodorotimer.action.STOP"
        const val EXTRA_REMAINING = "remaining_seconds"
        const val EXTRA_PHASE = "phase"
    }
}
```

---

### Task 10: TimerScreen (Main UI)

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/ui/timer/TimerScreen.kt`

- [ ] **Step 1: Create TimerScreen.kt**

```kotlin
package com.pomodorotimer.ui.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pomodorotimer.ui.stats.StatsPanel
import com.pomodorotimer.ui.stats.StatsViewModel
import com.pomodorotimer.ui.settings.SettingsPanel
import com.pomodorotimer.ui.settings.SettingsViewModel
import com.pomodorotimer.ui.theme.*
import com.pomodorotimer.ui.timer.components.CircularTimer
import com.pomodorotimer.ui.timer.components.Controls
import com.pomodorotimer.ui.timer.components.TimeDisplay
import com.pomodorotimer.ui.timer.components.TomatoDots

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

    // Phase color animation
    val phaseColor by animateColorAsState(
        targetValue = when (state.phase) {
            TimerPhase.FOCUS -> LightAccent
            TimerPhase.SHORT_BREAK -> if (isDarkTheme) DarkSecondary else LightSecondary
            TimerPhase.LONG_BREAK -> if (isDarkTheme) DarkPrimaryVariant else LightPrimaryVariant
        },
        label = "phaseColor"
    )

    // Calculate progress
    val totalSeconds = when (state.phase) {
        TimerPhase.FOCUS -> state.focusDuration * 60
        TimerPhase.SHORT_BREAK -> state.shortBreakDuration * 60
        TimerPhase.LONG_BREAK -> state.longBreakDuration * 60
    }
    val progress = if (totalSeconds > 0) state.remainingSeconds.toFloat() / totalSeconds else 1f

    // Determine font size based on screen width
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
            // Landscape: left timer + right info
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer side
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
                    TimeDisplay(
                        seconds = state.remainingSeconds,
                        fontSize = timeFontSize
                    )
                }

                // Info side
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
            // Portrait: vertical layout with height-based spacing
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
                // Progress ring
                val ringSize = when {
                    screenHeightDp < 600 -> 180.dp
                    screenHeightDp < 800 -> 240.dp
                    else -> 280.dp
                }

                CircularTimer(
                    progress = progress,
                    phaseColor = phaseColor,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.size(ringSize)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time display
                TimeDisplay(
                    seconds = state.remainingSeconds,
                    fontSize = timeFontSize
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tomato dots + count
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

                // Controls
                Controls(
                    isRunning = state.isRunning,
                    onStart = { timerViewModel.start() },
                    onPause = { timerViewModel.pause() },
                    onReset = { timerViewModel.reset() }
                )

                // Skip break button
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

    // Stats BottomSheet
    if (showStats) {
        ModalBottomSheet(onDismissRequest = { showStats = false }) {
            StatsPanel(
                viewModel = statsViewModel,
                onDismiss = { showStats = false }
            )
        }
    }

    // Settings BottomSheet
    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
            SettingsPanel(
                viewModel = settingsViewModel,
                onDismiss = { showSettings = false }
            )
        }
    }
}
```

---

### Task 11: Stats Panel (BottomSheet)

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/ui/stats/StatsPanel.kt`

- [ ] **Step 1: Create StatsPanel.kt**

```kotlin
package com.pomodorotimer.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatsPanel(
    viewModel: StatsViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            text = "统计",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("今日", "${state.todayCount}", "个番茄")
            StatItem("本周", "${state.weekData.sumOf { it.count }}", "个番茄")
            StatItem("总时长", "%.1f".format(state.totalFocusHours), "小时")
        }

        Spacer(Modifier.height(24.dp))

        // Week chart
        Text("本周", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.weekData.forEach { day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .width(24.dp)
                            .height((day.count * 8).dp.coerceAtLeast(4.dp)),
                        shape = MaterialTheme.shapes.small,
                        color = if (day.count > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ) {}
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
```

---

### Task 12: Settings Panel (BottomSheet)

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/ui/settings/SettingsPanel.kt`

- [ ] **Step 1: Create SettingsPanel.kt**

```kotlin
package com.pomodorotimer.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(
    viewModel: SettingsViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Duration settings
        DurationSetting("专注时长", state.focusDuration, 1, 120, "分钟") {
            viewModel.updateFocusDuration(it)
        }
        DurationSetting("短休时长", state.shortBreakDuration, 1, 30, "分钟") {
            viewModel.updateShortBreak(it)
        }
        DurationSetting("长休时长", state.longBreakDuration, 1, 60, "分钟") {
            viewModel.updateLongBreak(it)
        }
        DurationSetting("长休间隔", state.longBreakInterval, 2, 8, "个番茄") {
            viewModel.updateLongBreakInterval(it)
        }
        DurationSetting("每日目标", state.dailyGoal, 1, 24, "个番茄") {
            viewModel.updateDailyGoal(it)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Auto start toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("自动开始下一阶段", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = state.autoStart,
                onCheckedChange = { viewModel.updateAutoStart(it) }
            )
        }

        Spacer(Modifier.height(12.dp))

        // White noise selector
        Text("白噪音", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        val noiseOptions = listOf("关闭", "雨声", "海浪", "篝火", "白噪音")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            noiseOptions.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = state.whiteNoiseType == index,
                    onClick = { viewModel.updateWhiteNoise(index) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = noiseOptions.size
                    )
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationSetting(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("-", style = MaterialTheme.typography.titleMedium)
            }
            FilledTonalButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("+", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
```

---

### Task 13: MainActivity — Wire Everything Up

**Files:**
- Create: `静/app/src/main/java/com/pomodorotimer/MainActivity.kt`

- [ ] **Step 1: Create MainActivity.kt**

```kotlin
package com.pomodorotimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pomodorotimer.ui.theme.JingTheme
import com.pomodorotimer.ui.timer.TimerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JingTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimerScreen()
                }
            }
        }
    }
}
```

---

### Task 14: Service Integration in TimerViewModel

**Files:**
- Modify: `静/app/src/main/java/com/pomodorotimer/service/TimerService.kt`
- Modify: `静/app/src/main/java/com/pomodorotimer/ui/timer/TimerViewModel.kt`

This task connects the TimerService Foreground Service to the TimerViewModel so notifications update in real-time.

- [ ] **Step 1: Add service integration methods to TimerViewModel**

Add to `TimerViewModel.kt` after the existing `refreshDailyCount()` function:

```kotlin
    fun startService() {
        val context = getApplication<android.app.Application>()
        val intent = android.content.Intent(context, com.pomodorotimer.service.TimerService::class.java).apply {
            action = com.pomodorotimer.service.TimerService.ACTION_START_TIMER
            putExtra(com.pomodorotimer.service.TimerService.EXTRA_REMAINING, _uiState.value.remainingSeconds)
            putExtra(com.pomodorotimer.service.TimerService.EXTRA_PHASE, _uiState.value.phase.name.lowercase())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun updateServiceNotification() {
        val context = getApplication<android.app.Application>()
        val intent = android.content.Intent(context, com.pomodorotimer.service.TimerService::class.java).apply {
            action = com.pomodorotimer.service.TimerService.ACTION_UPDATE
            putExtra(com.pomodorotimer.service.TimerService.EXTRA_REMAINING, _uiState.value.remainingSeconds)
            putExtra(com.pomodorotimer.service.TimerService.EXTRA_PHASE, _uiState.value.phase.name.lowercase())
        }
        context.startService(intent)
    }

    fun stopService() {
        val context = getApplication<android.app.Application>()
        val intent = android.content.Intent(context, com.pomodorotimer.service.TimerService::class.java).apply {
            action = com.pomodorotimer.service.TimerService.ACTION_STOP
        }
        context.startService(intent)
    }
```

- [ ] **Step 2: Update `start()` function in TimerViewModel to also start foreground service**

Replace the existing `start()` function:

```kotlin
    fun start() {
        if (_uiState.value.isRunning) return
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
```

- [ ] **Step 3: Update `pause()` to stop service**

```kotlin
    fun pause() {
        tickJob?.cancel()
        stopService()
        _uiState.update { it.copy(isRunning = false) }
    }
```

- [ ] **Step 4: Update `reset()` to stop service**

```kotlin
    fun reset() {
        tickJob?.cancel()
        stopService()
        val state = _uiState.value
        val seconds = when (state.phase) {
            TimerPhase.FOCUS -> state.focusDuration * 60
            TimerPhase.SHORT_BREAK -> state.shortBreakDuration * 60
            TimerPhase.LONG_BREAK -> state.longBreakDuration * 60
        }
        _uiState.update { it.copy(remainingSeconds = seconds, isRunning = false) }
    }
```

- [ ] **Step 5: Update `setPhase()` to stop service**

```kotlin
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
```

---

### Task 15: Alert & Audio Integration in TimerViewModel

**Files:**
- Modify: `静/app/src/main/java/com/pomodorotimer/ui/timer/TimerViewModel.kt`

- [ ] **Step 1: Add AudioManager field and alert logic to TimerViewModel**

Add to the class body in TimerViewModel.kt:

```kotlin
    private val audioManager = com.pomodorotimer.service.AudioManager(application)
```

- [ ] **Step 2: Update `onPhaseComplete()` to play alert sound and vibrate**

Replace `private suspend fun onPhaseComplete()`:

```kotlin
    private suspend fun onPhaseComplete() {
        val state = _uiState.value
        _uiState.update { it.copy(isRunning = false) }
        stopService()

        // Play alert
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
            // Re-enable white noise for new focus if needed
            if (_uiState.value.phase == TimerPhase.FOCUS && state.whiteNoiseType > 0) {
                audioManager.playWhiteNoise(state.whiteNoiseType)
            }
            start()
        }
    }
```

- [ ] **Step 3: Update `start()` to also handle white noise**

```kotlin
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
```

- [ ] **Step 4: Add cleanup in `reset()`**

```kotlin
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
```

- [ ] **Step 5: Update `skipBreak()` to handle audio**

```kotlin
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
```

---

### Task 16: Self-Review & Cleanup

**Files:**
- Verify: all files listed in the project structure exist

- [ ] **Step 1: Verify project structure completeness**

```bash
# From the project root (静/)
# Expected structure:
find . -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.properties" | sort

# Expected output should include:
# ./app/build.gradle.kts
# ./app/src/main/AndroidManifest.xml
# ./app/src/main/java/com/pomodorotimer/MainActivity.kt
# ./app/src/main/java/com/pomodorotimer/PomodoroApp.kt
# ./app/src/main/java/com/pomodorotimer/data/local/AppDatabase.kt
# ./app/src/main/java/com/pomodorotimer/data/local/PomodoroDao.kt
# ./app/src/main/java/com/pomodorotimer/data/local/PomodoroRecord.kt
# ./app/src/main/java/com/pomodorotimer/data/preferences/SettingsStore.kt
# ./app/src/main/java/com/pomodorotimer/service/AudioManager.kt
# ./app/src/main/java/com/pomodorotimer/service/TimerService.kt
# ./app/src/main/java/com/pomodorotimer/ui/settings/SettingsPanel.kt
# ./app/src/main/java/com/pomodorotimer/ui/settings/SettingsViewModel.kt
# ./app/src/main/java/com/pomodorotimer/ui/stats/StatsPanel.kt
# ./app/src/main/java/com/pomodorotimer/ui/stats/StatsViewModel.kt
# ./app/src/main/java/com/pomodorotimer/ui/theme/Color.kt
# ./app/src/main/java/com/pomodorotimer/ui/theme/Theme.kt
# ./app/src/main/java/com/pomodorotimer/ui/theme/Type.kt
# ./app/src/main/java/com/pomodorotimer/ui/timer/TimerScreen.kt
# ./app/src/main/java/com/pomodorotimer/ui/timer/TimerViewModel.kt
# ./app/src/main/java/com/pomodorotimer/ui/timer/components/CircularTimer.kt
# ./app/src/main/java/com/pomodorotimer/ui/timer/components/Controls.kt
# ./app/src/main/java/com/pomodorotimer/ui/timer/components/TimeDisplay.kt
# ./app/src/main/java/com/pomodorotimer/ui/timer/components/TomatoDots.kt
# ./app/src/main/res/values/strings.xml
# ./build.gradle.kts
# ./gradle.properties
# ./gradle/wrapper/gradle-wrapper.properties
# ./settings.gradle.kts
```

- [ ] **Step 2: Verify imports reference existing files only**

Check that all imports in Kotlin files reference either Android SDK classes or classes defined in the app's own package (`com.pomodorotimer.*`).

- [ ] **Step 3: Clean up unused resources**

Remove any `mipmap/` default launcher icon references or create a minimal `ic_launcher` resource directory.

---

## Spec Coverage Check

| Spec Requirement | Task # | Status |
|---|---|---|
| 计时核心（专注/短休/长休，可自定义） | 4, 12 | ✅ |
| 开始/暂停/重置 | 4, 5 | ✅ |
| 阶段自动流转 | 4 | ✅ |
| 完成计数 + 进度点 | 4, 5 | ✅ |
| 通知提醒（锁屏计时） | 9, 14 | ✅ |
| 结束提醒（响铃+震动） | 8, 15 | ✅ |
| 统计（日/周/月 BottomSheet） | 6, 11 | ✅ |
| 白噪音 | 8, 15 | ✅ |
| 日式侘寂主题（深浅双模式） | 2 | ✅ |
| 屏幕适配（横竖屏 + 多比例） | 10 | ✅ |
| 自定义时长设置 | 7, 12 | ✅ |
