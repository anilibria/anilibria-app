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

    fun getAppTheme(): Single<String> = single {
        appThemeHolder.getTheme().toAnalyticsAppTheme().value
    }

    fun getQualitySettings(): Single<String> = single {
        preferencesHolder.getQuality().toAnalyticsQuality().value
    }

    fun getPlayerSettings(): Single<String> = single {
        preferencesHolder.getPlayerType().toAnalyticsPlayer().value
    }

    fun getPipSettings(): Single<String> = single {
        preferencesHolder.pipControl.toAnalyticsPip().value
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

    fun getAuthState(): Single<String> = single {
        userHolder.getUser().authState.toAnalyticsAuthState().value
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