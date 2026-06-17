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
