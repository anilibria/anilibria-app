package ru.radiationx.data.analytics.features.mapper

import ru.radiationx.data.analytics.features.model.*
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.AuthState

fun AuthState.toAnalyticsAuthState(): AnalyticsAuthState = when (this) {
    AuthState.NO_AUTH -> AnalyticsAuthState.NO
    AuthState.AUTH_SKIPPED -> AnalyticsAuthState.SKIP
    AuthState.AUTH -> AnalyticsAuthState.AUTH
    else -> AnalyticsAuthState.UNKNOWN
}

fun Int.toAnalyticsQuality(): AnalyticsQuality = when (this) {
    PreferencesHolder.QUALITY_SD -> AnalyticsQuality.SD
    PreferencesHolder.QUALITY_HD -> AnalyticsQuality.HD
    PreferencesHolder.QUALITY_FULL_HD -> AnalyticsQuality.FULL_HD
    PreferencesHolder.QUALITY_NO -> AnalyticsQuality.NONE
    PreferencesHolder.QUALITY_ALWAYS -> AnalyticsQuality.ALWAYS_ASK
    else -> AnalyticsQuality.UNKNOWN
}

fun Int.toAnalyticsPlayer(): AnalyticsPlayer = when (this) {
    PreferencesHolder.PLAYER_TYPE_EXTERNAL -> AnalyticsPlayer.EXTERNAL
    PreferencesHolder.PLAYER_TYPE_INTERNAL -> AnalyticsPlayer.INTERNAL
    PreferencesHolder.PLAYER_TYPE_NO -> AnalyticsPlayer.NONE
    PreferencesHolder.PLAYER_TYPE_ALWAYS -> AnalyticsPlayer.ALWAYS_ASK
    else -> AnalyticsPlayer.UNKNOWN
}

fun Int.toAnalyticsPip(): AnalyticsPip = when (this) {
    PreferencesHolder.PIP_AUTO -> AnalyticsPip.AUTO
    PreferencesHolder.PIP_BUTTON -> AnalyticsPip.BUTTON
    else -> AnalyticsPip.UNKNOWN
}

fun Int.toAnalyticsScale(): AnalyticsVideoScale = when (this) {
    0 -> AnalyticsVideoScale.CENTER
    1 -> AnalyticsVideoScale.CENTER_CROP
    2 -> AnalyticsVideoScale.CENTER_INSIDE
    3 -> AnalyticsVideoScale.FIT_CENTER
    4 -> AnalyticsVideoScale.FIT_XY
    5 -> AnalyticsVideoScale.NONE
    else -> AnalyticsVideoScale.UNKNOWN
}
