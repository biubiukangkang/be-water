package com.pomodorotimer.ui.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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
            modifier = Modifier.padding(bottom = 20.dp)
        )

        DurationSetting("专注", state.focusDuration, 1, 120, "分钟") {
            viewModel.updateFocusDuration(it)
        }
        DurationSetting("短休", state.shortBreakDuration, 1, 30, "分钟") {
            viewModel.updateShortBreak(it)
        }
        DurationSetting("长休", state.longBreakDuration, 1, 60, "分钟") {
            viewModel.updateLongBreak(it)
        }
        DurationSetting("每日目标", state.dailyGoal, 1, 24, "个") {
            viewModel.updateDailyGoal(it)
        }

        Divider(
            modifier = Modifier.padding(vertical = 14.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "自动继续",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            CustomToggle(
                checked = state.autoStart,
                onCheckedChange = { viewModel.updateAutoStart(it) }
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "白噪音",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        val noiseOptions = listOf("关闭", "雨声", "海浪", "篝火", "白噪音")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            noiseOptions.forEachIndexed { index, label ->
                val selected = state.whiteNoiseType == index
                NoiseChip(
                    label = label,
                    selected = selected,
                    onClick = { viewModel.updateWhiteNoise(index) },
                    modifier = Modifier.weight(1f)
                )
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
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassSettingButton(
                text = "−",
                onClick = { if (value > min) onValueChange(value - 1) },
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.widthIn(min = 28.dp),
                maxLines = 1
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            GlassSettingButton(
                text = "+",
                onClick = { if (value < max) onValueChange(value + 1) },
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun GlassSettingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        label = "settingBtnScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(4.dp, CircleShape)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)), CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun CustomToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        label = "toggleThumb"
    )

    Box(
        modifier = modifier
            .width(42.dp)
            .height(24.dp)
            .background(
                color = if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)),
                CircleShape
            )
            .clip(CircleShape)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterStart)
                .offset(x = thumbOffset)
                .background(
                    color = if (checked) Color.White else MaterialTheme.colorScheme.onBackground,
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun NoiseChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected)
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
    else MaterialTheme.colorScheme.primary
    val contentColor = if (selected) MaterialTheme.colorScheme.onBackground
    else MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)),
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}
