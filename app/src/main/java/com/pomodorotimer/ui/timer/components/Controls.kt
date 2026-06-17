package com.pomodorotimer.ui.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Controls(
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onReset,
            shape = CircleShape,
            modifier = Modifier.size(56.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "重置",
                modifier = Modifier.size(24.dp)
            )
        }

        Button(
            onClick = { if (isRunning) onPause() else onStart() },
            shape = CircleShape,
            modifier = Modifier.size(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "暂停" else "开始",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
