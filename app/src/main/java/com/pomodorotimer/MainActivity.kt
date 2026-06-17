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
