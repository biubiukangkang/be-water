package com.pomodorotimer.ui.timer.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pomodorotimer.ui.theme.ProgressComplete
import com.pomodorotimer.ui.theme.ProgressEmpty
import com.pomodorotimer.ui.theme.ProgressEmptyDark

@Composable
fun TomatoDots(
    completedCount: Int,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayCount = dailyGoal.coerceIn(1, 12)
        for (i in 0 until displayCount) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = if (i < completedCount) ProgressComplete
                else if (MaterialTheme.colorScheme.background == com.pomodorotimer.ui.theme.DarkBackground)
                    ProgressEmptyDark
                else ProgressEmpty
            ) {}
        }
    }
}
