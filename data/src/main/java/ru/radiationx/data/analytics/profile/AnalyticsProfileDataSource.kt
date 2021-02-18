package ru.radiationx.data.analytics.profile

import io.reactivex.Single
import ru.radiationx.data.analytics.features.model.*
import ru.radiationx.data.entity.common.AuthState

interface AnalyticsProfileDataSource {

    fun getApiAddress(): Single<String>

    fun getAppTheme(): Single<AnalyticsAppTheme>

    fun getQualitySettings(): Single<AnalyticsQuality>

    fun getPlayerSettings(): Single<AnalyticsPlayer>

    fun getPipSettings(): Single<AnalyticsPip>

    fun getPlaySpeedSettings(): Single<Float>

    fun getNotificationsAllSettings(): Single<Boolean>

    fun getNotificationsServiceSettings(): Single<Boolean>

    fun getEpisodeOrderSettings(): Single<Boolean>

    fun getAuthState(): Single<AnalyticsAuthState>

    fun getHistoryItemsCount(): Single<Int>

    fun getEpisodesItemsCount(): Single<Int>

    fun getDownloadsCount(): Single<Int>
}