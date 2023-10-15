@file:Suppress("RemoveExplicitTypeArguments", "RemoveExplicitTypeArguments")

package ru.radiationx.data.analytics.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.radiationx.data.analytics.features.mapper.toAnalyticsAuthState
import ru.radiationx.data.analytics.features.mapper.toAnalyticsPip
import ru.radiationx.data.analytics.features.mapper.toAnalyticsPlayer
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.datasource.holders.DownloadsHolder
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.data.repository.AuthRepository
import toothpick.InjectConstructor

@InjectConstructor
class AnalyticsProfileDataSource(
    private val preferencesHolder: PreferencesHolder,
    private val analyticsThemeProvider: AnalyticsThemeProvider,
    private val apiConfig: ApiConfig,
    private val historyHolder: HistoryHolder,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
    private val downloadsHolder: DownloadsHolder,
    private val migrationDataSource: MigrationDataSource,
    private val releaseUpdateHolder: ReleaseUpdateHolder,
    private val authRepository: AuthRepository,
) {

    fun getApiAddressTag(): Flow<String> = single {
        apiConfig.tag
    }

    fun getAppTheme(): Flow<String> = single {
        analyticsThemeProvider.getTheme().value
    }

    fun getQualitySettings(): Flow<String> = single {
        preferencesHolder.getQuality().toAnalyticsQuality().value
    }

    fun getPlayerSettings(): Flow<String> = single {
        preferencesHolder.getPlayerType().toAnalyticsPlayer().value
    }

    fun getPipSettings(): Flow<String> = single {
        preferencesHolder.pipControl.toAnalyticsPip().value
    }

    fun getPlaySpeedSettings(): Flow<Float> = single {
        preferencesHolder.playSpeed
    }

    fun getNotificationsAllSettings(): Flow<Boolean> = single {
        preferencesHolder.notificationsAll
    }

    fun getNotificationsServiceSettings(): Flow<Boolean> = single {
        preferencesHolder.notificationsService
    }

    fun getEpisodeOrderSettings(): Flow<Boolean> = single {
        preferencesHolder.episodesIsReverse
    }

    fun getAuthState(): Flow<String> = single {
        authRepository.getAuthState().toAnalyticsAuthState().value
    }

    fun getHistoryItemsCount(): Flow<Int> = single {
        historyHolder.getEpisodes().size
    }

    fun getEpisodesItemsCount(): Flow<Int> = single {
        episodesCheckerHolder.getEpisodes().size
    }

    fun getReleasesItemsCount(): Flow<Int> = single {
        releaseUpdateHolder.getReleases().size
    }

    fun getDownloadsCount(): Flow<Int> = single {
        downloadsHolder.getDownloads().size
    }

    fun getAppVersionsHistory(): Flow<String> = single {
        migrationDataSource.getHistory().joinToString()
    }

    private fun <T> single(callable: suspend () -> T) = flow {
        emit(callable.invoke())
    }
}