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
    val dayLabel: String,
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

            cal.timeInMillis = now
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val todayCount = dao.getCompletedCountForDay(dayStart, dayStart + 86400000L)

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
