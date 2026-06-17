package com.pomodorotimer.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pomodorotimer.MainActivity
import com.pomodorotimer.PomodoroApp

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
