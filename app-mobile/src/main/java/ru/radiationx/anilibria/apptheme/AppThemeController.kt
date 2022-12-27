package ru.radiationx.anilibria.apptheme

import kotlinx.coroutines.flow.Flow

interface AppThemeController {
    fun observeTheme(): Flow<AppTheme>
    fun getTheme(): AppTheme

    fun observeMode(): Flow<AppThemeMode>
    fun getMode(): AppThemeMode
    fun setMode(mode: AppThemeMode)
}