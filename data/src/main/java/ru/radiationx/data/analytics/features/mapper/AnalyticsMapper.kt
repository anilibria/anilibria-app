package ru.radiationx.data.analytics.features.mapper

import ru.radiationx.data.analytics.features.model.*
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.AuthState
import java.lang.IllegalStateException

fun AppThemeHolder.AppTheme.toAnalyticsAppTheme(): AnalyticsAppTheme = when (this) {
    AppThemeHolder.AppTheme.LIGHT -> AnalyticsAppTheme.LIGHT
    AppThemeHolder.AppTheme.DARK -> AnalyticsAppTheme.DARK
}

fun AuthState.toAnalyticsAuthState(): AnalyticsAuthState = when (this) {
    AuthState.NO_AUTH -> AnalyticsAuthState.NO
    AuthState.AUTH_SKIPPED -> AnalyticsAuthState.SKIP
    AuthState.AUTH -> AnalyticsAuthState.AUTH
}

fun Int.toAnalyticsQuality(): AnalyticsQuality = when (this) {
    PreferencesHolder.QUALITY_SD -> AnalyticsQuality.SD
    PreferencesHolder.QUALITY_HD -> AnalyticsQuality.HD
    PreferencesHolder.QUALITY_FULL_HD -> AnalyticsQuality.FULL_HD
    PreferencesHolder.QUALITY_NO -> AnalyticsQuality.NONE
    PreferencesHolder.QUALITY_ALWAYS -> AnalyticsQuality.ALWAYS_ASK
    else -> throw IllegalStateException("Unknown quality $this")
}

fun Int.toAnalyticsPlayer(): AnalyticsPlayer = when (this) {
    PreferencesHolder.PLAYER_TYPE_EXTERNAL -> AnalyticsPlayer.EXTERNAL
    PreferencesHolder.PLAYER_TYPE_INTERNAL -> AnalyticsPlayer.INTERNAL
    PreferencesHolder.PLAYER_TYPE_NO -> AnalyticsPlayer.NONE
    PreferencesHolder.PLAYER_TYPE_ALWAYS -> AnalyticsPlayer.NONE
    else -> throw IllegalStateException("Unknown player $this")
}

fun Int.toAnalyticsPip(): AnalyticsPip = when (this) {
    PreferencesHolder.PIP_AUTO -> AnalyticsPip.AUTO
    PreferencesHolder.PIP_BUTTON -> AnalyticsPip.BUTTON
    else -> throw IllegalStateException("Unknown pip $this")
}
