@file:Suppress("RemoveExplicitTypeArguments", "RemoveExplicitTypeArguments")

package ru.radiationx.data.analytics.profile

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.mapper.toAnalyticsAuthState
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.datasource.holders.DownloadsHolder
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.data.repository.AuthRepository
import javax.inject.Inject

class AnalyticsProfileDataSource @Inject constructor(
    private val preferencesHolder: PreferencesHolder,
    private val analyticsThemeProvider: AnalyticsThemeProvider,
    private val apiConfig: ApiConfig,
    private val historyHolder: HistoryHolder,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
    private val downloadsHolder: DownloadsHolder,
    private val migrationDataSource: MigrationDataSource,
    private val releaseUpdateHolder: ReleaseUpdateHolder,
    private val authRepository: AuthRepository,
    private val sharedBuildConfig: SharedBuildConfig
) {

    suspend fun getAttributes(): List<ProfileAttribute> = coroutineScope {
        val attributes = listOf(
            asyncAttr(ProfileConstants.address_tag) {
                apiConfig.tag.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.app_theme) {
                analyticsThemeProvider.getTheme().value.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.quality) {
                preferencesHolder.playerQuality.value.toAnalyticsQuality().value.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.play_speed) {
                preferencesHolder.playSpeed.value.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.notification_all) {
                preferencesHolder.notificationsAll.value.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.notification_service) {
                preferencesHolder.notificationsService.value.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.episode_order) {
                preferencesHolder.episodesIsReverse.value.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.auth_state) {
                authRepository.getAuthState().toAnalyticsAuthState().value.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.history_count) {
                historyHolder.getIds().size.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.episodes_count) {
                episodesCheckerHolder.getEpisodes().size.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.releases_count) {
                releaseUpdateHolder.getReleases().size.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.downloads_count) {
                downloadsHolder.getDownloads().size.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.app_versions) {
                migrationDataSource.getHistory().joinToString().mapToAttr(it)
            },
            asyncAttr(ProfileConstants.has_ads) {
                sharedBuildConfig.hasAds.mapToAttr(it)
            }
        )
        attributes.awaitAll()
    }
}