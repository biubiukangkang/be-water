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
