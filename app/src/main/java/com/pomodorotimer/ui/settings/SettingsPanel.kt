package com.pomodorotimer.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(
    viewModel: SettingsViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        DurationSetting("专注时长", state.focusDuration, 1, 120, "分钟") {
            viewModel.updateFocusDuration(it)
        }
        DurationSetting("短休时长", state.shortBreakDuration, 1, 30, "分钟") {
            viewModel.updateShortBreak(it)
        }
        DurationSetting("长休时长", state.longBreakDuration, 1, 60, "分钟") {
            viewModel.updateLongBreak(it)
        }
        DurationSetting("长休间隔", state.longBreakInterval, 2, 8, "个番茄") {
            viewModel.updateLongBreakInterval(it)
        }
        DurationSetting("每日目标", state.dailyGoal, 1, 24, "个番茄") {
            viewModel.updateDailyGoal(it)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("自动开始下一阶段", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = state.autoStart,
                onCheckedChange = { viewModel.updateAutoStart(it) }
            )
        }

        Spacer(Modifier.height(12.dp))

        Text("白噪音", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        val noiseOptions = listOf("关闭", "雨声", "海浪", "篝火", "白噪音")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            noiseOptions.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = state.whiteNoiseType == index,
                    onClick = { viewModel.updateWhiteNoise(index) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = noiseOptions.size
                    )
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun DurationSetting(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("-", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FilledTonalButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Text("+", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
