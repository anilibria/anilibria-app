package ru.radiationx.data.analytics.profile

import io.reactivex.Single
import ru.radiationx.data.analytics.features.mapper.*
import ru.radiationx.data.analytics.features.model.*
import ru.radiationx.data.datasource.holders.*
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.AuthState
import toothpick.InjectConstructor

@InjectConstructor
class AnalyticsProfileDataSource(
    private val preferencesHolder: PreferencesHolder,
    private val appThemeHolder: AppThemeHolder,
    private val apiConfig: ApiConfig,
    private val userHolder: UserHolder,
    private val historyHolder: HistoryHolder,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
    private val downloadsHolder: DownloadsHolder
) {

    fun getApiAddressTag(): Single<String> = single {
        apiConfig.tag
    }

    fun getAppTheme(): Single<AnalyticsAppTheme> = single {
        appThemeHolder.getTheme().toAnalyticsAppTheme()
    }

    fun getQualitySettings(): Single<AnalyticsQuality> = single {
        preferencesHolder.getQuality().toAnalyticsQuality()
    }

    fun getPlayerSettings(): Single<AnalyticsPlayer> = single {
        preferencesHolder.getPlayerType().toAnalyticsPlayer()
    }

    fun getPipSettings(): Single<AnalyticsPip> = single {
        preferencesHolder.pipControl.toAnalyticsPip()
    }

    fun getPlaySpeedSettings(): Single<Float> = single {
        preferencesHolder.playSpeed
    }

    fun getNotificationsAllSettings(): Single<Boolean> = single {
        preferencesHolder.notificationsAll
    }

    fun getNotificationsServiceSettings(): Single<Boolean> = single {
        preferencesHolder.notificationsService
    }

    fun getEpisodeOrderSettings(): Single<Boolean> = single {
        preferencesHolder.getEpisodesIsReverse()
    }

    fun getAuthState(): Single<AnalyticsAuthState> = single {
        userHolder.getUser().authState.toAnalyticsAuthState()
    }

    fun getHistoryItemsCount(): Single<Int> = historyHolder
        .getEpisodes()
        .map { it.size }

    fun getEpisodesItemsCount(): Single<Int> = episodesCheckerHolder
        .getEpisodes()
        .map { it.size }

    fun getDownloadsCount(): Single<Int> = single {
        downloadsHolder.getDownloads().size
    }

    private fun <T> single(callable: () -> T) = Single.fromCallable(callable)
}