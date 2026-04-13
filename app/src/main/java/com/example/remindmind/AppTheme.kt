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

/**
 * Класс для хранения цветов темы оформления приложения.
 *
 * Содержит все необходимые цвета для различных элементов интерфейса:
 * основные цвета, фоновые, текстовые, для градиентов и карточек.
 *
 * @property primary Основной цвет темы
 * @property secondary Вторичный цвет (акцентный)
 * @property background Цвет фона экранов
 * @property surface Цвет поверхностей (карточек, диалогов)
 * @property text Основной цвет текста
 * @property textSecondary Вторичный цвет текста (для подписей)
 * @property border Цвет границ элементов
 * @property buttonGradientStart Начальный цвет градиента кнопок
 * @property buttonGradientEnd Конечный цвет градиента кнопок
 * @property cardBackground Цвет фона карточек задач
 * @property statusBarColor Цвет строки состояния
 * @property boxtext Цвет текста в полях ввода
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0 (базовые цвета), 1.2.1 (улучшение тем от Максима)
 * @version 2.2.0
 */
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

/**
 * Цветовая схема светлой темы.
 *
 * Использует белый фон и черный текст с акцентным бирюзовым цветом.
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 */
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

/**
 * Цветовая схема темной темы.
 *
 * Использует темно-серый фон и белый текст для комфортного использования в темноте.
 *
 * @author Грехов М.В. (улучшение тем)
 * @since 1.2.1
 */
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

/**
 * Цветовая схема темно-синей (Navy) темы.
 *
 * Фирменная тема приложения с насыщенным синим фоном и бирюзовыми акцентами.
 * Используется по умолчанию.
 *
 * @author Грехов М.В., Яньшина А.Ю. (база)
 * @since 1.0.0 (базовая), 1.2.1 (улучшенная)
 */
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

/**
 * Перечисление доступных типов тем оформления.
 *
 * @property LIGHT Светлая тема
 * @property DARK Темная тема
 * @property NAVY Темно-синяя тема (по умолчанию)
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 */
enum class ThemeType {
    LIGHT,
    DARK,
    NAVY
}

/**
 * CompositionLocal для передачи текущей темы через Compose-дерево.
 *
 * Позволяет дочерним компонентам получать доступ к цветам текущей темы
 * без явной передачи через параметры.
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 */
val LocalAppColors = staticCompositionLocalOf { LightThemeColors }

/**
 * Возвращает цветовую схему для указанного типа темы.
 *
 * @param themeType Тип темы (LIGHT, DARK, NAVY)
 * @return Объект AppColors с цветами для выбранной темы
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 */
@Composable
fun getCurrentTheme(themeType: ThemeType): AppColors {
    return when (themeType) {
        ThemeType.LIGHT -> LightThemeColors
        ThemeType.DARK -> DarkThemeColors
        ThemeType.NAVY -> NavyThemeColors
    }
}

/**
 * DataStore для сохранения выбранной темы.
 *
 * Использует Preferences DataStore для хранения настроек темы
 * между запусками приложения.
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 */
private val Context.dataStore by preferencesDataStore("settings")

/**
 * Класс для управления настройками темы.
 *
 * Предоставляет Flow для отслеживания текущей темы и методы для её сохранения.
 *
 * @property context Контекст приложения для доступа к DataStore
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 */
class ThemePreferences(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
    }

    /**
     * Flow с текущей выбранной темой.
     *
     * При изменении настроек темы Flow автоматически обновляется.
     * По умолчанию возвращает NAVY тему.
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    val themeFlow: Flow<ThemeType> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: ThemeType.NAVY.name
            ThemeType.valueOf(themeName)
        }

    /**
     * Сохраняет выбранную тему в DataStore.
     *
     * @param theme Тип темы для сохранения
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    suspend fun saveTheme(theme: ThemeType) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}