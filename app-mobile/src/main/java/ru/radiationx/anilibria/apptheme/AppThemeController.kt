package ru.radiationx.anilibria.apptheme

import io.reactivex.Observable

interface AppThemeController {
    fun observeTheme(): Observable<AppTheme>
    fun getTheme(): AppTheme

    fun observeMode(): Observable<AppThemeMode>
    fun getMode(): AppThemeMode
    fun setMode(mode: AppThemeMode)
}