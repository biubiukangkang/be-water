package com.pomodorotimer.ui.timer.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pomodorotimer.ui.theme.DarkBackground
import com.pomodorotimer.ui.theme.ProgressComplete
import com.pomodorotimer.ui.theme.ProgressEmpty
import com.pomodorotimer.ui.theme.ProgressEmptyDark

@Composable
fun TomatoDots(
    completedCount: Int,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    val displayCount = dailyGoal.coerceIn(1, 12)

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

    LaunchedEffect(animatingIndex) {
        if (animatingIndex >= 0) {
            kotlinx.coroutines.delay(350)
            animatingIndex = -1
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until displayCount) {
            val isCompleted = i < completedCount
            val targetScale = if (isCompleted) 1f else 1f
            val shouldPop = i == animatingIndex

            val scale by animateFloatAsState(
                targetValue = if (shouldPop) targetScale else targetScale,
                animationSpec = if (shouldPop) {
                    keyframes {
                        durationMillis = 350
                        0f at 0
                        1.5f at 210
                        1f at 350
                    }
                } else {
                    androidx.compose.animation.core.snap()
                },
                label = "dotPop_$i"
            )

            val dotColor = if (isCompleted) ProgressComplete
            else if (MaterialTheme.colorScheme.background == DarkBackground)
                ProgressEmptyDark
            else ProgressEmpty

            Surface(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                shape = CircleShape,
                color = dotColor
            ) {}
        }
    }
}
