package com.example.remindmind

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/**
 * Диалог создания новой подзадачи.
 *
 * Позволяет ввести текст подзадачи, выбрать приоритет, дату и время,
 * а также настроить уведомление для подзадачи.
 *
 * @param viewModel ViewModel для управления данными
 * @param onDismiss Callback закрытия диалога
 *
 * @author Яньшина А.Ю.
 * @since 1.2.0
 * @version 2.0.0 (добавлены настройки уведомлений), 2.1.1 (исправлено закрытие окна при ошибке)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTaskDialog(
    viewModel: RemindersViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var showNotificationSettings by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.subtask_title),
                color = colors.text
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Поле ввода текста подзадачи
                OutlinedTextField(
                    value = viewModel.subTaskText,
                    onValueChange = { viewModel.subTaskText = it },
                    label = { Text(stringResource(id = R.string.subtask_text_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.secondary,
                        unfocusedTextColor = colors.secondary,
                        focusedBorderColor = colors.secondary,
                        unfocusedBorderColor = colors.textSecondary
                    )
                )

                // Выбор приоритета (добавлен в версии 1.2.0)
                Text(
                    text = stringResource(id = R.string.priority),
                    color = colors.text,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityChipSubTask(
                        text = stringResource(id = R.string.priority_high),
                        isSelected = viewModel.subTaskPriority == Priority.HIGH,
                        onClick = { viewModel.subTaskPriority = Priority.HIGH },
                        colors = colors
                    )
                    PriorityChipSubTask(
                        text = stringResource(id = R.string.priority_medium),
                        isSelected = viewModel.subTaskPriority == Priority.MEDIUM,
                        onClick = { viewModel.subTaskPriority = Priority.MEDIUM },
                        colors = colors
                    )
                    PriorityChipSubTask(
                        text = stringResource(id = R.string.priority_low),
                        isSelected = viewModel.subTaskPriority == Priority.LOW,
                        onClick = { viewModel.subTaskPriority = Priority.LOW },
                        colors = colors
                    )
                }

                // Выбор даты и времени
                SubTaskDatePicker(viewModel)
                SubTaskTimePicker(viewModel)

                // Кнопка настройки уведомлений (добавлена в версии 2.0.0)
                Button(
                    onClick = { showNotificationSettings = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.secondary.copy(alpha = 0.2f)
                    )
                ) {
                    Text("⚙️ Настроить уведомление", color = colors.text)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.currentReminderForSubTask?.let { reminder ->
                        viewModel.addSubTask(context, reminder)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    Color(0xFF03DAC5)
                )
            ) {
                Text(stringResource(id = R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel), color = colors.textSecondary)
            }
        },
        containerColor = colors.surface,
        titleContentColor = colors.text,
        textContentColor = colors.text
    )

    // Диалог настроек уведомлений для подзадачи (добавлен в версии 2.0.0)
    if (showNotificationSettings) {
        NotificationSettingsDialog(
            currentSettings = viewModel.subTaskNotificationSettings,
            onSettingsChanged = { settings ->
                viewModel.subTaskNotificationSettings = settings
            },
            onDismiss = { showNotificationSettings = false }
        )
    }
}

/**
 * Чип выбора приоритета для подзадачи.
 *
 * @param text Текст чипа
 * @param isSelected Выбран ли чип
 * @param onClick Callback при нажатии
 * @param colors Цветовая схема
 *
 * @author Яньшина А.Ю.
 * @since 1.2.0
 */
@Composable
fun PriorityChipSubTask(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF03DAC5) else colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else colors.text,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Поле выбора даты для подзадачи.
 *
 * @param viewModel ViewModel для управления данными
 *
 * @author Яньшина А.Ю.
 * @since 1.2.0
 */
@Composable
fun SubTaskDatePicker(viewModel: RemindersViewModel) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val newDate = "${Utils.addZero(selectedDay)}.${Utils.addZero(selectedMonth + 1)}.$selectedYear"

            // Проверяем и очищаем время если нужно
            if (viewModel.subTaskTime.isNotEmpty() && isSubTaskToday(newDate)) {
                val currentTime = Calendar.getInstance()
                val selectedHour = viewModel.subTaskTime.split(":")[0].toInt()
                val selectedMinute = viewModel.subTaskTime.split(":")[1].toInt()

                if (selectedHour < currentTime.get(Calendar.HOUR_OF_DAY) ||
                    (selectedHour == currentTime.get(Calendar.HOUR_OF_DAY) &&
                            selectedMinute < currentTime.get(Calendar.MINUTE))) {
                    viewModel.subTaskTime = ""
                }
            }

            viewModel.subTaskDate = newDate
        },
        year, month, day
    ).apply {
        datePicker.minDate = calendar.timeInMillis
    }

    OutlinedTextField(
        value = viewModel.subTaskDate,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        label = {
            Text(
                text = stringResource(id = R.string.form_date_hint),
                color = colors.boxtext
            )
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.form_date_hint),
                color = colors.boxtext
            )
        },
        textStyle = TextStyle(color = colors.text),
        enabled = false,
        readOnly = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.secondary,
            unfocusedTextColor = colors.text,
            focusedBorderColor = colors.secondary,
            unfocusedBorderColor = colors.textSecondary,
            focusedLabelColor = colors.secondary,
            unfocusedLabelColor = colors.textSecondary,
            cursorColor = colors.secondary
        )
    )
}

/**
 * Поле выбора времени для подзадачи.
 *
 * @param viewModel ViewModel для управления данными
 *
 * @author Яньшина А.Ю.
 * @since 1.2.0
 */
@Composable
fun SubTaskTimePicker(viewModel: RemindersViewModel) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val errorMessage = stringResource(id = R.string.time_cannot_be_past)

    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val dateForValidation = if (viewModel.subTaskDate.isEmpty()) {
                val today = Calendar.getInstance()
                "${Utils.addZero(today.get(Calendar.DAY_OF_MONTH))}.${Utils.addZero(today.get(Calendar.MONTH) + 1)}.${today.get(Calendar.YEAR)}"
            } else {
                viewModel.subTaskDate
            }

            if (isSubTaskTimeValid(dateForValidation, selectedHour, selectedMinute)) {
                if (viewModel.subTaskDate.isEmpty()) {
                    val today = Calendar.getInstance()
                    viewModel.subTaskDate = "${Utils.addZero(today.get(Calendar.DAY_OF_MONTH))}.${Utils.addZero(today.get(Calendar.MONTH) + 1)}.${today.get(Calendar.YEAR)}"
                }
                viewModel.subTaskTime = "${Utils.addZero(selectedHour)}:${Utils.addZero(selectedMinute)}"
            } else {
                android.widget.Toast.makeText(
                    context,
                    errorMessage,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        },
        hour, minute, true
    )

    OutlinedTextField(
        value = viewModel.subTaskTime,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                timePickerDialog.show()
            },
        label = {
            Text(
                text = stringResource(id = R.string.form_time_hint),
                color = colors.boxtext
            )
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.form_time_hint),
                color = colors.boxtext
            )
        },
        textStyle = TextStyle(color = colors.text),
        enabled = false,
        readOnly = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.secondary,
            unfocusedTextColor = colors.text,
            focusedBorderColor = colors.secondary,
            unfocusedBorderColor = colors.textSecondary,
            focusedLabelColor = colors.secondary,
            unfocusedLabelColor = colors.textSecondary,
            cursorColor = colors.secondary
        )
    )
}

/**
 * Проверяет, можно ли установить указанное время для подзадачи.
 *
 * @param date Дата в формате dd.MM.yyyy
 * @param hour Выбранный час
 * @param minute Выбранная минута
 * @return true, если время допустимо (не в прошлом)
 *
 * @author Грехов М.В. (адаптировано для подзадач)
 * @since 1.2.1
 */
private fun isSubTaskTimeValid(date: String, hour: Int, minute: Int): Boolean {
    if (date.isEmpty()) return false

    if (isSubTaskToday(date)) {
        val currentCalendar = Calendar.getInstance()
        val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentCalendar.get(Calendar.MINUTE)

        return if (hour > currentHour) {
            true
        } else if (hour == currentHour) {
            minute >= currentMinute
        } else {
            false
        }
    }
    return true
}

/**
 * Проверяет, является ли указанная дата сегодняшним днём для подзадачи.
 *
 * @param date Дата в формате dd.MM.yyyy
 * @return true, если дата совпадает с сегодняшней
 *
 * @author Грехов М.В. (адаптировано для подзадач)
 * @since 1.2.1
 */
private fun isSubTaskToday(date: String): Boolean {
    if (date.isEmpty()) return false

    try {
        val parts = date.split(".")
        if (parts.size == 3) {
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)

            return day == currentDay && month == currentMonth && year == currentYear
        }
    } catch (e: Exception) {
        return false
    }
    return false
}