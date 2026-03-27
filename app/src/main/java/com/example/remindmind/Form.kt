//package com.example.remindmind
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.widget.DatePicker
//import android.widget.TimePicker
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import java.util.Calendar
//
//@Composable
//fun Form(viewModel: RemindersViewModel = viewModel()) {
//    val colors = LocalAppColors.current
//    val context = LocalContext.current
//
//    Column(
//        modifier = Modifier
//            .padding(10.dp)
//            .clip(RoundedCornerShape(15.dp))
//            .background(colors.surface)
//            .border(0.5.dp, colors.border, RoundedCornerShape(15.dp))
//            .padding(top = 10.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = stringResource(id = R.string.form_title),
//            style = TextStyle(
//                color = colors.text,
//                fontSize = 20.sp,
//                fontFamily = FontFamily.SansSerif,
//                fontWeight = FontWeight.Thin
//            )
//        )
//
//        ReminderTextField(viewModel)
//        DateTimeInputFields(viewModel)
//        CreateButton {
//            viewModel.addReminder(context)
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ReminderTextField(viewModel: RemindersViewModel) {
//    val colors = LocalAppColors.current
//
//    TextField(
//        value = viewModel.text,
//        onValueChange = { viewModel.text = it },
//        label = { Text(text = stringResource(id = R.string.form_text_hint)) },
//        colors = TextFieldDefaults.colors(
//            focusedTextColor = colors.secondary,
//            unfocusedTextColor = colors.secondary,
//            cursorColor = colors.secondary,
//            focusedLabelColor = colors.secondary,
//            unfocusedLabelColor = colors.textSecondary,
//            focusedContainerColor = Color.Transparent,
//            unfocusedContainerColor = Color.Transparent,
//            focusedIndicatorColor = Color.Transparent,
//            unfocusedIndicatorColor = Color.Transparent,
//            disabledIndicatorColor = Color.Transparent
//        ),
//        singleLine = true,
//        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
//        modifier = Modifier.fillMaxWidth()
//    )
//}
//
//@Composable
//fun DateTimeInputFields(viewModel: RemindersViewModel) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        DateInputField(viewModel)
//        TimeInputField(viewModel)
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DateInputField(viewModel: RemindersViewModel) {
//    val colors = LocalAppColors.current
//    val context = LocalContext.current
//    val calendar = Calendar.getInstance()
//    val year = calendar.get(Calendar.YEAR)
//    val month = calendar.get(Calendar.MONTH)
//    val day = calendar.get(Calendar.DAY_OF_MONTH)
//    val datePickerDialog = DatePickerDialog(
//        context,
//        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
//            viewModel.date = "${Utils.addZero(selectedDay)}.${Utils.addZero(selectedMonth + 1)}.$selectedYear"
//        },
//        year, month, day
//    )
//
//    Box {
//        TextField(
//            value = viewModel.date.ifEmpty { "" },
//            onValueChange = { viewModel.date = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { datePickerDialog.show() },
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = colors.surface,
//                unfocusedContainerColor = colors.surface,
//                disabledContainerColor = colors.surface,
//                focusedTextColor = colors.secondary,
//                unfocusedTextColor = colors.secondary,
//                disabledTextColor = colors.secondary
//            ),
//            enabled = false,
//            readOnly = true,
//            placeholder = {
//                Text(
//                    text = stringResource(id = R.string.form_date_hint),
//                    color = colors.secondary
//                )
//            }
//        )
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TimeInputField(viewModel: RemindersViewModel) {
//    val colors = LocalAppColors.current
//    val context = LocalContext.current
//    val calendar = Calendar.getInstance()
//    val hour = calendar.get(Calendar.HOUR_OF_DAY)
//    val minute = calendar.get(Calendar.MINUTE)
//    val timePickerDialog = TimePickerDialog(
//        context,
//        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
//            viewModel.time = "${Utils.addZero(selectedHour)}:${Utils.addZero(selectedMinute)}"
//        },
//        hour, minute, true
//    )
//
//    Box {
//        TextField(
//            value = "",
//            onValueChange = { viewModel.time = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { timePickerDialog.show() },
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = colors.surface,
//                unfocusedContainerColor = colors.surface,
//                disabledContainerColor = colors.surface,
//                disabledTextColor = Color.Transparent
//            ),
//            enabled = false,
//            readOnly = true,
//            placeholder = {}
//        )
//
//        Text(
//            text = if (viewModel.time.isNotEmpty()) viewModel.time
//            else stringResource(id = R.string.form_time_hint),
//            color = colors.secondary,
//            modifier = Modifier
//                .align(Alignment.CenterStart)
//                .padding(start = 10.dp)
//        )
//    }
//}
//
//@Composable
//fun CreateButton(onClick: () -> Unit) {
//    val colors = LocalAppColors.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    Button(
//        onClick = {
//            onClick()
//            keyboardController?.hide()
//        },
//        modifier = Modifier
//            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
//            .fillMaxWidth()
//            .background(
//                brush = Brush.horizontalGradient(
//                    colors = listOf(
//                        colors.buttonGradientStart,
//                        colors.buttonGradientEnd
//                    )
//                ),
//                shape = RoundedCornerShape(15.dp)
//            ),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color.Transparent
//        )
//    ) {
//        Text(
//            stringResource(id = R.string.form_create),
//            style = TextStyle(
//                color = colors.text,
//                fontWeight = FontWeight.Bold
//            )
//        )
//    }
//}
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
        CreateButtonComponent {
            viewModel.addReminder(context)
        }
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
                    selectedContainerColor = colors.secondary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = viewModel.selectedPriority == Priority.MEDIUM,
                onClick = { viewModel.selectedPriority = Priority.MEDIUM },
                label = { Text(stringResource(id = R.string.priority_medium)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.secondary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = viewModel.selectedPriority == Priority.LOW,
                onClick = { viewModel.selectedPriority = Priority.LOW },
                label = { Text(stringResource(id = R.string.priority_low)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.secondary,
                    selectedLabelColor = Color.White
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
            viewModel.date = "${Utils.addZero(selectedDay)}.${Utils.addZero(selectedMonth + 1)}.$selectedYear"
        },
        year, month, day
    )

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
                    color = colors.secondary
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
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            viewModel.time = "${Utils.addZero(selectedHour)}:${Utils.addZero(selectedMinute)}"
        },
        hour, minute, true
    )

    Box {
        TextField(
            value = "",
            onValueChange = { viewModel.time = it },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { timePickerDialog.show() },
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
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
        )
    }
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