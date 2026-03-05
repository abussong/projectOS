package com.example.remindmind
/*
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remindmind.ui.theme.RemindmindTheme
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.remindmind.AppColors
import com.example.remindmind.LocalAppColors
import com.example.remindmind.ThemeType
import com.example.remindmind.ThemePreferences
import com.example.remindmind.getCurrentTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean -> if(!isGranted) {
        Toast.makeText(this, R.string.permission_warning, Toast.LENGTH_LONG).show()
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.BLACK

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }


        setContent {
            val systemUiController: SystemUiController = rememberSystemUiController()
            systemUiController.isSystemBarsVisible = false
            systemUiController.isNavigationBarVisible = false
            systemUiController.isSystemBarsVisible = false

            val viewModel: RemindersViewModel = viewModel()

            viewModel.dbHelper = DatabaseHelper(LocalContext.current)
            viewModel.alarmManager = alarmManager

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.black),
                                colorResource(id = R.color.navy)
                            )
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppTitle()
                Form(viewModel)
                List(viewModel)
            }
        }
    }
}

@Composable
fun AppTitle() {
    Text(
        text = stringResource(id = R.string.app_title),
        style = TextStyle(
            color = colorResource(id = R.color.white),
            fontSize = 26.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Thin
        )
    )
}*/

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean -> if(!isGranted) {
        Toast.makeText(this, R.string.permission_warning, Toast.LENGTH_LONG).show()
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }

        setContent {
            val context = LocalContext.current
            val themePreferences = remember { ThemePreferences(context) }
            val currentTheme by themePreferences.themeFlow.collectAsState(initial = ThemeType.NAVY)

            val colors = getCurrentTheme(currentTheme)

            // Обновляем статус бар
            LaunchedEffect(currentTheme) {
                window.statusBarColor = colors.statusBarColor.toArgb()
            }

            CompositionLocalProvider(LocalAppColors provides colors) {
                val viewModel: RemindersViewModel = viewModel()

                LaunchedEffect(Unit) {
                    viewModel.dbHelper = DatabaseHelper(context)
                    viewModel.alarmManager = alarmManager
                }

                MainScreen(
                    viewModel = viewModel,
                    onSettingsClick = {
                        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: RemindersViewModel,
    onSettingsClick: () -> Unit
) {
    val colors = LocalAppColors.current

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
        // Верхняя панель с заголовком и кнопкой настроек
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTitle()

            // Кнопка настроек
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
        List(viewModel)
    }
}

@Composable
fun AppTitle() {
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