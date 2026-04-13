package com.example.remindmind

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.widget.Toast

/**
 * Диалог настройки уведомлений для задачи или подзадачи.
 *
 * Позволяет настроить:
 * - Периодичность (один раз, ежедневно, еженедельно)
 * - Дни недели для еженедельного повтора
 * - Звук уведомления (выбор из системных)
 * - Вибрацию (вкл/выкл)
 * - Количество и интервал дополнительных повторов
 *
 * @param currentSettings Текущие настройки уведомления
 * @param onSettingsChanged Callback при изменении настроек
 * @param onDismiss Callback закрытия диалога
 *
 * @author Яньшина А.Ю.
 * @since 2.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsDialog(
    currentSettings: NotificationSettings,
    onSettingsChanged: (NotificationSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current

    // Получаем строки из ресурсов
    val soundSelectedMsg = stringResource(R.string.sound_selected)
    val resetToDefaultMsg = stringResource(R.string.reset_to_default_sound)
    val selectSoundTitle = stringResource(R.string.select_sound_title)
    val defaultSoundText = stringResource(R.string.default_sound)
    val customSoundText = stringResource(R.string.custom_sound)
    val selectedText = stringResource(R.string.selected)

    var repeatType by remember { mutableStateOf(currentSettings.repeatType) }
    var selectedDays by remember { mutableStateOf(currentSettings.selectedDays.toMutableSet()) }
    var isVibrationEnabled by remember { mutableStateOf(currentSettings.isVibrationEnabled) }
    var repeatCount by remember { mutableStateOf(currentSettings.repeatCount) }
    var repeatInterval by remember { mutableStateOf(currentSettings.repeatIntervalMinutes) }
    var selectedSoundUri by remember { mutableStateOf(currentSettings.soundUri) }

    var uiUpdateTrigger by remember { mutableStateOf(0) }

    fun forceUpdateUI() {
        uiUpdateTrigger++
    }

    // Лаунчер для выбора звука из системного пикера
    val soundPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { data ->
            val pickedUri = data.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (pickedUri != null) {
                android.util.Log.d("NotificationSettings", "Picked sound URI: $pickedUri")
                selectedSoundUri = pickedUri
                forceUpdateUI()
                Toast.makeText(context, soundSelectedMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.notification_settings_title),
                color = colors.text,
                fontSize = 20.sp
            )
        },
        text = {
            key(uiUpdateTrigger) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ==================== Выбор периодичности ====================
                    item {
                        Text(
                            text = stringResource(R.string.repeat_period),
                            color = colors.text,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = repeatType == RepeatType.ONCE,
                                    onClick = {
                                        repeatType = RepeatType.ONCE
                                        forceUpdateUI()
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.secondary
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.repeat_once),
                                    color = colors.text,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = repeatType == RepeatType.DAILY,
                                    onClick = {
                                        repeatType = RepeatType.DAILY
                                        forceUpdateUI()
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.secondary
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.repeat_daily),
                                    color = colors.text,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = repeatType == RepeatType.WEEKLY,
                                    onClick = {
                                        repeatType = RepeatType.WEEKLY
                                        forceUpdateUI()
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.secondary
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.repeat_weekly),
                                    color = colors.text,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // ==================== Выбор дней недели (для еженедельного повтора) ====================
                    if (repeatType == RepeatType.WEEKLY) {
                        item(key = "weekly_days_$uiUpdateTrigger") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surface.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.select_days),
                                        color = colors.text,
                                        fontSize = 14.sp
                                    )

                                    // Первая строка: Пн, Вт, Ср, Чт
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        WeekDay.values().take(4).forEach { day ->
                                            val isSelected = selectedDays.contains(day)
                                            val dayText = when (day) {
                                                WeekDay.MONDAY -> stringResource(R.string.mon_short)
                                                WeekDay.TUESDAY -> stringResource(R.string.tue_short)
                                                WeekDay.WEDNESDAY -> stringResource(R.string.wed_short)
                                                WeekDay.THURSDAY -> stringResource(R.string.thu_short)
                                                else -> ""
                                            }
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    if (selectedDays.contains(day)) {
                                                        selectedDays.remove(day)
                                                    } else {
                                                        selectedDays.add(day)
                                                    }
                                                    forceUpdateUI()
                                                },
                                                label = {
                                                    Text(
                                                        text = dayText,
                                                        fontSize = 11.sp,
                                                        maxLines = 1
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                            )
                                        }
                                    }

                                    // Вторая строка: Пт, Сб, Вс
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        WeekDay.values().drop(4).forEach { day ->
                                            val isSelected = selectedDays.contains(day)
                                            val dayText = when (day) {
                                                WeekDay.FRIDAY -> stringResource(R.string.fri_short)
                                                WeekDay.SATURDAY -> stringResource(R.string.sat_short)
                                                WeekDay.SUNDAY -> stringResource(R.string.sun_short)
                                                else -> ""
                                            }
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    if (selectedDays.contains(day)) {
                                                        selectedDays.remove(day)
                                                    } else {
                                                        selectedDays.add(day)
                                                    }
                                                    forceUpdateUI()
                                                },
                                                label = {
                                                    Text(
                                                        text = dayText,
                                                        fontSize = 11.sp,
                                                        maxLines = 1
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==================== Выбор звука ====================
                    item(key = "sound_section_$selectedSoundUri") {
                        Text(
                            text = stringResource(R.string.notification_sound),
                            color = colors.text,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Button(
                            onClick = {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, selectSoundTitle)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedSoundUri)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                }
                                soundPickerLauncher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.select_sound))
                        }

                        val soundInfo = if (selectedSoundUri == Settings.System.DEFAULT_NOTIFICATION_URI) {
                            defaultSoundText
                        } else {
                            val fileName = selectedSoundUri.lastPathSegment ?: selectedText
                            "$customSoundText $fileName"
                        }

                        Text(
                            text = soundInfo,
                            color = if (selectedSoundUri != Settings.System.DEFAULT_NOTIFICATION_URI) colors.secondary else colors.textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (selectedSoundUri != Settings.System.DEFAULT_NOTIFICATION_URI) {
                            TextButton(
                                onClick = {
                                    selectedSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI
                                    forceUpdateUI()
                                    Toast.makeText(context, resetToDefaultMsg, Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(stringResource(R.string.reset_to_default))
                            }
                        }
                    }

                    // ==================== Вибрация ====================
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.vibration),
                                color = colors.text,
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = isVibrationEnabled,
                                onCheckedChange = {
                                    isVibrationEnabled = it
                                    forceUpdateUI()
                                }
                            )
                        }
                    }

                    // ==================== Повторы уведомления ====================
                    item {
                        Text(
                            text = stringResource(R.string.notification_repeats),
                            color = colors.text,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.repeat_count),
                                color = colors.text,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = repeatCount.toString(),
                                onValueChange = { value ->
                                    val newValue = value.toIntOrNull()
                                    if (newValue != null && newValue in 0..10) {
                                        repeatCount = newValue
                                        forceUpdateUI()
                                    }
                                },
                                modifier = Modifier.width(80.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = colors.secondary,
                                    unfocusedTextColor = colors.text,
                                    focusedBorderColor = colors.secondary,
                                    unfocusedBorderColor = colors.textSecondary
                                )
                            )
                        }

                        if (repeatCount > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.repeat_interval_minutes),
                                    color = colors.text,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = repeatInterval.toString(),
                                    onValueChange = { value ->
                                        val newValue = value.toIntOrNull()
                                        if (newValue != null && newValue in 1..60) {
                                            repeatInterval = newValue
                                            forceUpdateUI()
                                        }
                                    },
                                    modifier = Modifier.width(80.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = colors.secondary,
                                        unfocusedTextColor = colors.text,
                                        focusedBorderColor = colors.secondary,
                                        unfocusedBorderColor = colors.textSecondary
                                    )
                                )
                            }

                            Text(
                                text = stringResource(R.string.interval_hint),
                                color = colors.textSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalInterval = if (repeatInterval < 1) 1 else repeatInterval
                    android.util.Log.d("NotificationSettings", "Saving sound URI: $selectedSoundUri")
                    onSettingsChanged(
                        NotificationSettings(
                            repeatType = repeatType,
                            selectedDays = selectedDays,
                            soundUri = selectedSoundUri,
                            isVibrationEnabled = isVibrationEnabled,
                            repeatCount = repeatCount,
                            repeatIntervalMinutes = finalInterval
                        )
                    )
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.textSecondary)
            }
        },
        containerColor = colors.surface
    )
}