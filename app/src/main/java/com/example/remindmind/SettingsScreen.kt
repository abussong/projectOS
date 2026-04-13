package com.example.remindmind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val themePreferences = remember { ThemePreferences(context) }
            val currentTheme by themePreferences.themeFlow.collectAsState(initial = ThemeType.NAVY)

            val colors = getCurrentTheme(currentTheme)

            CompositionLocalProvider(LocalAppColors provides colors) {
                SettingsScreen(
                    currentTheme = currentTheme,
                    onThemeSelected = { theme ->
                        lifecycleScope.launch {
                            themePreferences.saveTheme(theme)
                        }
                    },
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: ThemeType,
    onThemeSelected: (ThemeType) -> Unit,
    onBackPressed: () -> Unit
) {
    val colors = LocalAppColors.current
    var showAboutScreen by remember { mutableStateOf(false) }

    if (showAboutScreen) {
        AboutScreen(
            onBackPressed = { showAboutScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.settings_title),
                            color = colors.text
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.text
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.primary,
                        titleContentColor = colors.text,
                        navigationIconContentColor = colors.text
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.theme_settings),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.text,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    ThemeOption(
                        title = stringResource(id = R.string.theme_light),
                        isSelected = currentTheme == ThemeType.LIGHT,
                        colors = colors,
                        onClick = { onThemeSelected(ThemeType.LIGHT) }
                    )
                }

                item {
                    ThemeOption(
                        title = stringResource(id = R.string.theme_dark),
                        isSelected = currentTheme == ThemeType.DARK,
                        colors = colors,
                        onClick = { onThemeSelected(ThemeType.DARK) }
                    )
                }

                item {
                    ThemeOption(
                        title = stringResource(id = R.string.theme_navy),
                        isSelected = currentTheme == ThemeType.NAVY,
                        colors = colors,
                        onClick = { onThemeSelected(ThemeType.NAVY) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.about_section),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.text,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutScreen = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.about_app),
                                color = colors.text,
                                fontSize = 16.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    isSelected: Boolean,
    colors: AppColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.secondary.copy(alpha = 0.2f)
            else colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = colors.text
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = colors.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackPressed: () -> Unit
) {
    val colors = LocalAppColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.about_app),
                        color = colors.text
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.primary,
                    titleContentColor = colors.text,
                    navigationIconContentColor = colors.text
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Название приложения (тонкий шрифт, как в заголовке)
            Text(
                text = stringResource(id = R.string.app_title),
                fontSize = 32.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Thin,
                color = colors.text
            )

            Text(
                text = stringResource(id = R.string.app_version, "2.0.1"),
                fontSize = 14.sp,
                color = colors.textSecondary
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.border
            )

            // Информация о разработчиках
            Text(
                text = stringResource(id = R.string.developers),
                fontSize = 18.sp,
                color = colors.text,
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Грехов М.В.",
                    fontSize = 16.sp,
                    color = colors.text
                )
                Text(
                    text = "Яньшина А.Ю.",
                    fontSize = 16.sp,
                    color = colors.text
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.border
            )

            // Описание проекта
            Text(
                text = stringResource(id = R.string.project_description),
                fontSize = 14.sp,
                color = colors.text,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(id = R.string.thanks_message),
                fontSize = 14.sp,
                color = colors.text,
                modifier = Modifier.fillMaxWidth()
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.border
            )

            // Контакты
            Text(
                text = stringResource(id = R.string.contacts),
                fontSize = 18.sp,
                color = colors.text,
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactItem(
                    email = "im.grexov.m@gmail.com",
                    colors = colors
                )
                ContactItem(
                    email = "nasyanshi@gmail.com",
                    colors = colors
                )
            }
        }
    }
}

@Composable
fun ContactItem(
    email: String,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📧",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = email,
            fontSize = 14.sp,
            color = colors.secondary
        )
    }
}