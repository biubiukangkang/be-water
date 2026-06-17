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
