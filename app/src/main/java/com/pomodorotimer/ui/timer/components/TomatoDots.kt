package com.pomodorotimer.ui.timer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pomodorotimer.ui.theme.RingCompleteDark
import com.pomodorotimer.ui.theme.RingCompleteLight
import com.pomodorotimer.ui.theme.RingTrackDark
import com.pomodorotimer.ui.theme.RingTrackLight
import kotlinx.coroutines.delay

@Composable
fun TomatoDots(
    completedCount: Int,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    val displayCount = dailyGoal.coerceIn(1, 12)
    val isDark = isSystemInDarkTheme()

    var previousCount by remember { mutableIntStateOf(0) }
    var animatingIndex by remember { mutableIntStateOf(-1) }
    var initial by remember { mutableStateOf(true) }

    LaunchedEffect(completedCount) {
        if (!initial && completedCount > previousCount) {
            animatingIndex = completedCount - 1
        }
        initial = false
        previousCount = completedCount
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until displayCount) {
            val isCompleted = i < completedCount
            val shouldPop = i == animatingIndex

            val scale = remember { Animatable(1f) }
            LaunchedEffect(shouldPop) {
                if (shouldPop) {
                    scale.snapTo(0f)
                    scale.animateTo(1.5f, tween(210))
                    scale.animateTo(1f, tween(140))
                }
            }

            val dotColor = if (isCompleted) {
                if (isDark) RingCompleteDark else RingCompleteLight
            } else {
                if (isDark) RingTrackDark else RingTrackLight
            }

            Surface(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
                shape = CircleShape,
                color = dotColor
            ) {}
        }
    }
}
