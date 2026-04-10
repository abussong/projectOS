package com.example.remindmind

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReminderItem(
    reminder: Reminder,
    viewModel: RemindersViewModel,
    onAddSubTask: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(reminder.isCompleted) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Синхронизируем состояние с моделью
    LaunchedEffect(reminder.isCompleted) {
        checked = reminder.isCompleted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) colors.cardBackground.copy(alpha = 0.5f)
            else colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { isChecked ->
                            checked = isChecked
                            viewModel.toggleReminderCompletion(reminder, context)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.secondary,
                            uncheckedColor = colors.textSecondary
                        )
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEditDialog = true }
                    ) {
                        Text(
                            text = reminder.text,
                            color = if (checked) colors.textSecondary else colors.text,
                            fontSize = 16.sp,
                            fontWeight = if (reminder.priority == Priority.HIGH) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "${reminder.date} ${reminder.time}",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                        PriorityIndicator(priority = reminder.priority, colors = colors)
                    }
                }

                Row {
                    IconButton(
                        onClick = onAddSubTask,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(text = "+", color = colors.secondary, fontSize = 20.sp)
                    }

                    IconButton(
                        onClick = { viewModel.removeReminder(reminder, context) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(text = "✕", color = colors.secondary, fontSize = 18.sp)
                    }

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = if (expanded) "▼" else "▶",
                            color = colors.secondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = colors.border)
                Spacer(modifier = Modifier.height(8.dp))

                if (reminder.subTasks.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        reminder.subTasks.forEach { subTask ->
                            SubTaskItem(
                                subTask = subTask,
                                reminder = reminder,
                                viewModel = viewModel
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.no_subtasks),
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        EditReminderDialog(
            reminder = reminder,
            onDismiss = { showEditDialog = false },
            onSave = { newText ->
                viewModel.editReminderText(reminder, newText, context)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun SubTaskItem(
    subTask: SubTask,
    reminder: Reminder,
    viewModel: RemindersViewModel
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var checked by remember { mutableStateOf(subTask.isCompleted) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Синхронизируем состояние с моделью
    LaunchedEffect(subTask.isCompleted) {
        checked = subTask.isCompleted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { showEditDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { isChecked ->
                    checked = isChecked
                    viewModel.toggleSubTaskCompletion(reminder, subTask, context)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.secondary,
                    uncheckedColor = colors.textSecondary
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subTask.text,
                    color = if (checked) colors.textSecondary else colors.text,
                    fontSize = 14.sp
                )
                Text(
                    text = "${subTask.date} ${subTask.time}",
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
                PriorityIndicator(priority = subTask.priority, colors = colors, small = true)
            }
        }

        IconButton(
            onClick = { viewModel.removeSubTask(reminder, subTask, context) },
            modifier = Modifier.size(28.dp)
        ) {
            Text(text = "✕", color = colors.secondary, fontSize = 14.sp)
        }
    }

    if (showEditDialog) {
        EditSubTaskDialog(
            subTask = subTask,
            reminder = reminder,
            onDismiss = { showEditDialog = false },
            onSave = { newText ->
                viewModel.editSubTaskText(reminder, subTask, newText, context)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditReminderDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val colors = LocalAppColors.current
    var text by remember { mutableStateOf(reminder.text) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.edit_task),
                color = colors.text
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.task_text)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.secondary,
                    unfocusedTextColor = colors.text,
                    focusedBorderColor = colors.secondary,
                    unfocusedBorderColor = colors.textSecondary,
                    focusedLabelColor = colors.secondary,
                    unfocusedLabelColor = colors.textSecondary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onSave(text) },
                colors = ButtonDefaults.buttonColors(colors.secondary)
            ) {
                Text(stringResource(R.string.save), color = Color.Black)
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

@Composable
fun EditSubTaskDialog(
    subTask: SubTask,
    reminder: Reminder,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val colors = LocalAppColors.current
    var text by remember { mutableStateOf(subTask.text) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.edit_subtask),
                color = colors.text
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.subtask_text)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.secondary,
                    unfocusedTextColor = colors.text,
                    focusedBorderColor = colors.secondary,
                    unfocusedBorderColor = colors.textSecondary,
                    focusedLabelColor = colors.secondary,
                    unfocusedLabelColor = colors.textSecondary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onSave(text) },
                colors = ButtonDefaults.buttonColors(colors.secondary)
            ) {
                Text(stringResource(R.string.save), color = Color.Black)
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

@Composable
fun PriorityIndicator(priority: Priority, colors: AppColors, small: Boolean = false) {
    val color = when (priority) {
        Priority.HIGH -> Color.Red
        Priority.MEDIUM -> Color.Yellow
        Priority.LOW -> Color.Green
    }

    Row(
        modifier = Modifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (small) 6.dp else 8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = when (priority) {
                Priority.HIGH -> stringResource(id = R.string.priority_high)
                Priority.MEDIUM -> stringResource(id = R.string.priority_medium)
                Priority.LOW -> stringResource(id = R.string.priority_low)
            },
            color = colors.textSecondary,
            fontSize = if (small) 10.sp else 11.sp
        )
    }
}