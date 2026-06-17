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
