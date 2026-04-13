package com.example.remindmind

import android.R.attr.delay
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

/**
 * Главное Activity приложения.
 *
 * Отвечает за отображение списка напоминаний, формы создания новых задач
 * и управление разрешениями на уведомления и точные будильники.
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 * @version 2.2.0
 */
class MainActivity : ComponentActivity() {

    /**
     * Лаунчер для запроса разрешения на отправку уведомлений (Android 13+).
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, R.string.permission_warning, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Вызывается при создании Activity.
     *
     * Инициализирует UI, проверяет разрешения на уведомления и точные будильники,
     * настраивает тему и загружает напоминания из базы данных.
     *
     * @param savedInstanceState Сохраненное состояние Activity или null
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Запрос разрешения на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !NotificationManagerCompat.from(this).areNotificationsEnabled()
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Запрос разрешения на точные будильники для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !alarmManager.canScheduleExactAlarms()
        ) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }

        setContent {
            val context = LocalContext.current
            val themePreferences = remember { ThemePreferences(context) }
            val currentTheme by themePreferences.themeFlow.collectAsState(initial = ThemeType.NAVY)

            val colors = getCurrentTheme(currentTheme)

            LaunchedEffect(currentTheme) {
                window.statusBarColor = colors.statusBarColor.toArgb()
            }

            CompositionLocalProvider(LocalAppColors provides colors) {
                val viewModel: RemindersViewModel = viewModel()

                LaunchedEffect(Unit) {
                    viewModel.dbHelper = DatabaseHelper(context)
                    viewModel.alarmManager = alarmManager
                    viewModel.getReminders(context)
                    delay(1000)
                    viewModel.cleanExpiredCompletedReminders(context)
                }

                MainScreenContent(
                    viewModel = viewModel,
                    onSettingsClick = {
                        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                )

                if (viewModel.showSubTaskDialog) {
                    SubTaskDialog(
                        viewModel = viewModel,
                        onDismiss = { viewModel.showSubTaskDialog = false }
                    )
                }
            }
        }
    }
}

/**
 * Основной экран приложения.
 *
 * Содержит заголовок, форму создания задач и список напоминаний.
 *
 * @param viewModel ViewModel для управления данными
 * @param onSettingsClick Callback для открытия экрана настроек
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 */
@Composable
fun MainScreenContent(
    viewModel: RemindersViewModel,
    onSettingsClick: () -> Unit
) {
    val colors = LocalAppColors.current
    var selectedReminder by remember { mutableStateOf<Reminder?>(null) }

    val reminders by remember { derivedStateOf { viewModel.reminders.toList() } }

    LaunchedEffect(selectedReminder) {
        if (selectedReminder != null) {
            viewModel.currentReminderForSubTask = selectedReminder
            viewModel.showSubTaskDialog = true
            selectedReminder = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.background,
                        colors.primary
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTitleText()

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "⚙️",
                    color = colors.text,
                    fontSize = 20.sp
                )
            }
        }

        Form(viewModel)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(reminders) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        viewModel = viewModel,
                        onAddSubTask = { selectedReminder = reminder }
                    )
                }
            }
        }
    }
}

/**
 * Заголовок приложения.
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 */
@Composable
fun AppTitleText() {
    val colors = LocalAppColors.current

    Text(
        text = stringResource(id = R.string.app_title),
        style = TextStyle(
            color = colors.text,
            fontSize = 26.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Thin
        )
    )
}