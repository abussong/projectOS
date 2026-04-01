package com.example.remindmind

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.remindmind.ui.theme.white
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Класс для хранения цветов темы
@Stable
class AppColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val border: Color,
    val buttonGradientStart: Color,
    val buttonGradientEnd: Color,
    val cardBackground: Color,
    val statusBarColor: Color,
    val boxtext: Color
)

// Предопределенные темы
val LightThemeColors = AppColors(
    primary = white, // navy
    secondary = Color(0xFF000000), // teal_200
    background = white,
    surface = white,
    text = Color(0xFF000000),
    textSecondary = Color(0xFF5E6995), // lightnavy
    border = Color(0xFF03DAC5), // teal_200
    buttonGradientStart = Color(0xFF03DAC5), // teal_200
    buttonGradientEnd = Color(0xFF5E6995), // lightnavy
    cardBackground = Color(0xFFF0F0F0),
    statusBarColor = Color(0xFF242B5F),
    boxtext = Color(0xFF000000)// navy
)

val DarkThemeColors = AppColors(
    primary = Color(0xFF121212), // navy
    secondary = white, // teal_200
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    text = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB0B0B0),
    border = Color(0xFF03DAC5), // teal_200
    buttonGradientStart = Color(0xFF03DAC5), // teal_200
    buttonGradientEnd = Color(0xFF5E6995), // lightnavy
    cardBackground = Color(0xFF2D2D2D),
    statusBarColor = Color(0xFF000000),
    boxtext = white
)

val NavyThemeColors = AppColors(
    primary = Color(0xFF242B5F), // navy
    secondary = Color(0xFF03DAC5), // teal_200
    background = Color(0xFF121731), // darknavy
    surface = Color(0xFF242B5F), // navy
    text = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF03DAC5), // teal_200
    border = Color(0xFF03DAC5), // teal_200
    buttonGradientStart = Color(0xFF03DAC5), // teal_200
    buttonGradientEnd = Color(0xFF5E6995), // lightnavy
    cardBackground = Color(0xFF242B5F), // navy
    statusBarColor = Color(0xFF000000),
    boxtext = Color(0xFF03DAC5)
)

// Enum для типов тем
enum class ThemeType {
    LIGHT,
    DARK,
    NAVY
}

// CompositionLocal для передачи темы
val LocalAppColors = staticCompositionLocalOf { LightThemeColors }

// Функция для получения текущей темы
@Composable
fun getCurrentTheme(themeType: ThemeType): AppColors {
    return when (themeType) {
        ThemeType.LIGHT -> LightThemeColors
        ThemeType.DARK -> DarkThemeColors
        ThemeType.NAVY -> NavyThemeColors
    }
}

//DataStore для сохранения выбранной темы
private val Context.dataStore by preferencesDataStore("settings")

class ThemePreferences(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
    }

    val themeFlow: Flow<ThemeType> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: ThemeType.NAVY.name
            ThemeType.valueOf(themeName)
        }

    suspend fun saveTheme(theme: ThemeType) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}