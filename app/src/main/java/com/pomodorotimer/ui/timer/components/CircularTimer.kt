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
import com.pomodorotimer.ui.theme.ProgressComplete
import com.pomodorotimer.ui.theme.ProgressEmpty
import com.pomodorotimer.ui.theme.ProgressEmptyDark

@Composable
fun CircularTimer(
    progress: Float,
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

        drawArc(
            color = if (isDarkTheme) ProgressEmptyDark else ProgressEmpty,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

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
