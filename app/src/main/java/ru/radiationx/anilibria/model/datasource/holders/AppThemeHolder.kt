package ru.radiationx.anilibria.model.datasource.holders

import io.reactivex.Observable

interface AppThemeHolder {
    fun observeTheme(): Observable<AppTheme>
    fun getTheme(): AppTheme
    enum class AppTheme { LIGHT, DARK }
}