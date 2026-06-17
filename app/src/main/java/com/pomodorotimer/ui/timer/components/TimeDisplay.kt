package com.pomodorotimer.ui.timer.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
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
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Light,
        letterSpacing = 4.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}
