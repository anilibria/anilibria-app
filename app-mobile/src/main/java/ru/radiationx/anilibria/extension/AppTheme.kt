package ru.radiationx.anilibria.extension

import androidx.annotation.StyleRes
import biz.source_code.miniTemplator.MiniTemplator
import ru.radiationx.anilibria.R
import ru.radiationx.data.datasource.holders.AppThemeHolder


@StyleRes
fun AppThemeHolder.AppTheme.getMainStyleRes() = when (this) {
    AppThemeHolder.AppTheme.LIGHT -> R.style.LightAppTheme_NoActionBar
    AppThemeHolder.AppTheme.DARK -> R.style.DarkAppTheme_NoActionBar
}

@StyleRes
fun AppThemeHolder.AppTheme.getPrefStyleRes() = when (this) {
    AppThemeHolder.AppTheme.LIGHT -> R.style.PreferencesLightAppTheme
    AppThemeHolder.AppTheme.DARK -> R.style.PreferencesDarkAppTheme
}

fun AppThemeHolder.AppTheme.getWebStyleType() = when (this) {
    AppThemeHolder.AppTheme.LIGHT -> "light"
    AppThemeHolder.AppTheme.DARK -> "dark"
}

fun AppThemeHolder.AppTheme.isDark() = when (this) {
    AppThemeHolder.AppTheme.LIGHT -> false
    AppThemeHolder.AppTheme.DARK -> true
}

fun MiniTemplator.generateWithTheme(appTheme: AppThemeHolder.AppTheme): String {
    this.setVariableOpt("app_theme", appTheme.getWebStyleType())
    return generateOutput().also {
        reset()
    }
}