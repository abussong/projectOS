package com.example.remindmind

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar

@Composable
fun Form(viewModel: RemindersViewModel = viewModel()) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var showNotificationSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(colors.surface)
            .border(0.5.dp, colors.border, RoundedCornerShape(15.dp))
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.form_title),
            style = TextStyle(
                color = colors.text,
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Thin
            )
        )

        ReminderTextField(viewModel)
        PrioritySelectorComponent(viewModel)
        DateTimeInputFieldsComponent(viewModel)

        Button(
            onClick = { showNotificationSettings = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.secondary.copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = "⚙ ${stringResource(R.string.configure_notification)}",
                color = colors.secondary,
                fontSize = 14.sp
            )
        }

        CreateButtonComponent {
            viewModel.addReminder(context)  // Проверка будет внутри метода
        }
    }

    if (showNotificationSettings) {
        NotificationSettingsDialog(
            currentSettings = viewModel.tempNotificationSettings,
            onSettingsChanged = { settings ->
                viewModel.tempNotificationSettings = settings
            },
            onDismiss = { showNotificationSettings = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTextField(viewModel: RemindersViewModel) {
    val colors = LocalAppColors.current

    TextField(
        value = viewModel.text,
        onValueChange = { viewModel.text = it },
        label = { Text(text = stringResource(id = R.string.form_text_hint)) },
        colors = TextFieldDefaults.colors(
            focusedTextColor = colors.secondary,
            unfocusedTextColor = colors.secondary,
            cursorColor = colors.secondary,
            focusedLabelColor = colors.secondary,
            unfocusedLabelColor = colors.textSecondary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PrioritySelectorComponent(viewModel: RemindersViewModel) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.priority),
            color = colors.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = viewModel.selectedPriority == Priority.HIGH,
                onClick = { viewModel.selectedPriority = Priority.HIGH },
                label = { Text(stringResource(id = R.string.priority_high)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF03DAC5),
                    selectedLabelColor = Color.White,
                    labelColor = colors.boxtext
                )
            )
            FilterChip(
                selected = viewModel.selectedPriority == Priority.MEDIUM,
                onClick = { viewModel.selectedPriority = Priority.MEDIUM },
                label = { Text(stringResource(id = R.string.priority_medium)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF03DAC5),
                    selectedLabelColor = Color.White,
                    labelColor = colors.boxtext
                )
            )
            FilterChip(
                selected = viewModel.selectedPriority == Priority.LOW,
                onClick = { viewModel.selectedPriority = Priority.LOW },
                label = { Text(stringResource(id = R.string.priority_low)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF03DAC5),
                    selectedLabelColor = Color.White,
                    labelColor = colors.boxtext
                )
            )
        }
    }
}

@Composable
fun DateTimeInputFieldsComponent(viewModel: RemindersViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DateInputFieldComponent(viewModel)
        TimeInputFieldComponent(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputFieldComponent(viewModel: RemindersViewModel) {
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
            if (viewModel.time.isNotEmpty() && isTodayForMain(newDate)) {
                val currentTime = Calendar.getInstance()
                val selectedHour = viewModel.time.split(":")[0].toInt()
                val selectedMinute = viewModel.time.split(":")[1].toInt()

                if (selectedHour < currentTime.get(Calendar.HOUR_OF_DAY) ||
                    (selectedHour == currentTime.get(Calendar.HOUR_OF_DAY) &&
                            selectedMinute < currentTime.get(Calendar.MINUTE))) {
                    viewModel.time = ""  // Просто очищаем поле времени
                }
            }

            viewModel.date = newDate
        },
        year, month, day
    ).apply {
        datePicker.minDate = calendar.timeInMillis
    }

    Box {
        TextField(
            value = viewModel.date.ifEmpty { "" },
            onValueChange = { viewModel.date = it },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                disabledContainerColor = colors.surface,
                focusedTextColor = colors.secondary,
                unfocusedTextColor = colors.secondary,
                disabledTextColor = colors.secondary
            ),
            enabled = false,
            readOnly = true,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.form_date_hint),
                    color = colors.secondary,
                    fontSize = 14.sp
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputFieldComponent(viewModel: RemindersViewModel) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val errorMessage = stringResource(id = R.string.time_cannot_be_past)
    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val dateForValidation = if (viewModel.date.isEmpty()) {
                val today = Calendar.getInstance()
                "${Utils.addZero(today.get(Calendar.DAY_OF_MONTH))}.${Utils.addZero(today.get(Calendar.MONTH) + 1)}.${today.get(Calendar.YEAR)}"
            } else {
                viewModel.date
            }

            if (isTimeValidForMain(dateForValidation, selectedHour, selectedMinute)) {
                if (viewModel.date.isEmpty()) {
                    val today = Calendar.getInstance()
                    viewModel.date = "${Utils.addZero(today.get(Calendar.DAY_OF_MONTH))}.${Utils.addZero(today.get(Calendar.MONTH) + 1)}.${today.get(Calendar.YEAR)}"
                }
                viewModel.time = "${Utils.addZero(selectedHour)}:${Utils.addZero(selectedMinute)}"
            } else {
                android.widget.Toast.makeText(
                    context,
                    errorMessage,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        },
        currentHour, currentMinute, true
    )

    Box {
        TextField(
            value = "",
            onValueChange = { viewModel.time = it },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (viewModel.date.isEmpty()) {
                        timePickerDialog.show()
                    } else {
                        if (isTodayForMain(viewModel.date)) {
                            val currentCalendar = Calendar.getInstance()
                            timePickerDialog.updateTime(
                                currentCalendar.get(Calendar.HOUR_OF_DAY),
                                currentCalendar.get(Calendar.MINUTE)
                            )
                        }
                        timePickerDialog.show()
                    }
                },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                disabledContainerColor = colors.surface,
                disabledTextColor = Color.Transparent
            ),
            enabled = false,
            readOnly = true,
            placeholder = {}
        )

        Text(
            text = if (viewModel.time.isNotEmpty()) viewModel.time
            else stringResource(id = R.string.form_time_hint),
            color = colors.secondary,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
    }
}

private fun isTimeValidForMain(date: String, hour: Int, minute: Int): Boolean {
    if (date.isEmpty()) return false

    if (isTodayForMain(date)) {
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

private fun isTodayForMain(date: String): Boolean {
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

@Composable
fun CreateButtonComponent(onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Button(
        onClick = {
            onClick()
            keyboardController?.hide()
        },
        modifier = Modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.buttonGradientStart,
                        colors.buttonGradientEnd
                    )
                ),
                shape = RoundedCornerShape(15.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        Text(
            text = stringResource(id = R.string.form_create),
            style = TextStyle(
                color = colors.text,
                fontWeight = FontWeight.Bold
            )
        )
    }
}