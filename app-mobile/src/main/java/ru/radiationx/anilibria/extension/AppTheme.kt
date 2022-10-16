package ru.radiationx.anilibria.extension

import biz.source_code.miniTemplator.MiniTemplator
import ru.radiationx.anilibria.apptheme.AppTheme


fun AppTheme.getWebStyleType() = when (this) {
    AppTheme.LIGHT -> "light"
    AppTheme.DARK -> "dark"
}

fun AppTheme.isDark() = when (this) {
    AppTheme.LIGHT -> false
    AppTheme.DARK -> true
}

fun MiniTemplator.generateWithTheme(appTheme: AppTheme): String {
    this.setVariableOpt("app_theme", appTheme.getWebStyleType())
    return generateOutput().also {
        reset()
    }
}