package ru.radiationx.anilibria.extension

import androidx.annotation.StyleRes
import biz.source_code.miniTemplator.MiniTemplator
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppThemeController


@StyleRes
fun AppThemeController.AppTheme.getMainStyleRes() = when (this) {
    AppThemeController.AppTheme.LIGHT -> R.style.DayNightAppTheme_NoActionBar
    AppThemeController.AppTheme.DARK -> R.style.DayNightAppTheme_NoActionBar
}

@StyleRes
fun AppThemeController.AppTheme.getPrefStyleRes() = when (this) {
    AppThemeController.AppTheme.LIGHT -> R.style.PreferencesDayNightAppTheme
    AppThemeController.AppTheme.DARK -> R.style.PreferencesDayNightAppTheme
}

fun AppThemeController.AppTheme.getWebStyleType() = when (this) {
    AppThemeController.AppTheme.LIGHT -> "light"
    AppThemeController.AppTheme.DARK -> "dark"
}

fun AppThemeController.AppTheme.isDark() = when (this) {
    AppThemeController.AppTheme.LIGHT -> false
    AppThemeController.AppTheme.DARK -> true
}

fun MiniTemplator.generateWithTheme(appTheme: AppThemeController.AppTheme): String {
    this.setVariableOpt("app_theme", appTheme.getWebStyleType())
    return generateOutput().also {
        reset()
    }
}