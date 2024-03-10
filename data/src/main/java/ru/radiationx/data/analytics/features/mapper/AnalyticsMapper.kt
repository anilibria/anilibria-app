package ru.radiationx.data.analytics.features.mapper

import ru.radiationx.data.analytics.features.model.AnalyticsAuthState
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.common.PlayerQuality

fun AuthState.toAnalyticsAuthState(): AnalyticsAuthState = when (this) {
    AuthState.NO_AUTH -> AnalyticsAuthState.NO
    AuthState.AUTH_SKIPPED -> AnalyticsAuthState.SKIP
    AuthState.AUTH -> AnalyticsAuthState.AUTH
}

fun PlayerQuality.toAnalyticsQuality(): AnalyticsQuality = when (this) {
    PlayerQuality.SD -> AnalyticsQuality.SD
    PlayerQuality.HD -> AnalyticsQuality.HD
    PlayerQuality.FULLHD -> AnalyticsQuality.FULL_HD
}
