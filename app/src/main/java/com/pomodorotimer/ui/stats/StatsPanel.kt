package com.pomodorotimer.ui.stats

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val PopEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

@Composable
fun StatsPanel(
    viewModel: StatsViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            text = "统计",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("今日", "${state.todayCount}", "个番茄")
            StatItem("本周", "${state.weekData.sumOf { it.count }}", "个番茄")
            StatItem("总时长", "%.1f".format(state.totalFocusHours), "小时")
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "本周",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            state.weekData.forEach { day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val targetHeight = (day.count * 8).dp.coerceAtLeast(4.dp)
                    val animatedHeight by animateDpAsState(
                        targetValue = targetHeight,
                        animationSpec = tween(500, easing = PopEasing),
                        label = "barHeight"
                    )
                    Surface(
                        modifier = Modifier
                            .width(24.dp)
                            .height(animatedHeight),
                        shape = MaterialTheme.shapes.small,
                        color = if (day.count > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ) {}
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.sp
        )
    }
}
