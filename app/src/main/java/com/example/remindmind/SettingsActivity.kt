package com.example.remindmind
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.foundation.background
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.launch
//
//class SettingsActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            val context = LocalContext.current
//            val themePreferences = remember { ThemePreferences(context) }
//            val currentTheme by themePreferences.themeFlow.collectAsState(initial = ThemeType.NAVY)
//
//            val colors = getCurrentTheme(currentTheme)
//
//            CompositionLocalProvider(LocalAppColors provides colors) {
//                SettingsScreen(
//                    currentTheme = currentTheme,
//                    onThemeSelected = { theme ->
//                        lifecycleScope.launch {
//                            themePreferences.saveTheme(theme)
//                        }
//                    },
//                    onBackPressed = { finish() }
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SettingsScreen(
//    currentTheme: ThemeType,
//    onThemeSelected: (ThemeType) -> Unit,
//    onBackPressed: () -> Unit
//) {
//    val colors = LocalAppColors.current
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Настройки",
//                        color = colors.text
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackPressed) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Назад",
//                            tint = colors.text
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = colors.primary,
//                    titleContentColor = colors.text,
//                    navigationIconContentColor = colors.text
//                )
//            )
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(colors.background)
//                .padding(paddingValues),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            item {
//                Text(
//                    text = "Тема оформления",
//                    style = MaterialTheme.typography.titleLarge,
//                    color = colors.text,
//                    modifier = Modifier.padding(bottom = 16.dp)
//                )
//            }
//
//            item {
//                ThemeOption(
//                    title = "Светлая",
//                    isSelected = currentTheme == ThemeType.LIGHT,
//                    colors = colors,
//                    onClick = { onThemeSelected(ThemeType.LIGHT) }
//                )
//            }
//
//            item {
//                ThemeOption(
//                    title = "Темная",
//                    isSelected = currentTheme == ThemeType.DARK,
//                    colors = colors,
//                    onClick = { onThemeSelected(ThemeType.DARK) }
//                )
//            }
//
//            item {
//                ThemeOption(
//                    title = "Темно-синяя (Navy)",
//                    isSelected = currentTheme == ThemeType.NAVY,
//                    colors = colors,
//                    onClick = { onThemeSelected(ThemeType.NAVY) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun ThemeOption(
//    title: String,
//    isSelected: Boolean,
//    colors: AppColors,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onClick() },
//        shape = RoundedCornerShape(8.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) colors.secondary.copy(alpha = 0.2f)
//            else colors.surface
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = title,
//                color = colors.text
//            )
//
//            if (isSelected) {
//                Text(
//                    text = "✓",
//                    color = colors.secondary,
//                    style = MaterialTheme.typography.titleMedium
//                )
//            }
//        }
//    }
//}