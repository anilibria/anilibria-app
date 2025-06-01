@file:Suppress("RemoveExplicitTypeArguments", "RemoveExplicitTypeArguments")

package ru.radiationx.data.analytics.profile

import android.os.Build
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.mapper.toAnalyticsAuthState
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.app.config.ApiConfig
import ru.radiationx.data.app.downloader.RemoteFileHolder
import ru.radiationx.data.app.episodeaccess.EpisodesCheckerHolder
import ru.radiationx.data.app.history.HistoryHolder
import ru.radiationx.data.app.preferences.PreferencesHolder
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateHolder
import ru.radiationx.data.migration.MigrationDataSource
import javax.inject.Inject

class AnalyticsMainProfileDataSource @Inject constructor(
    private val preferencesHolder: PreferencesHolder,
    private val analyticsThemeProvider: AnalyticsThemeProvider,
    private val apiConfig: ApiConfig,
    private val historyHolder: HistoryHolder,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
    private val remoteFileHolder: RemoteFileHolder,
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
                remoteFileHolder.getSize().mapToAttr(it)
            },
            asyncAttr(ProfileConstants.app_versions) {
                migrationDataSource.getHistory().joinToString().mapToAttr(it)
            },
            asyncAttr(ProfileConstants.has_ads) {
                sharedBuildConfig.hasAds.mapToAttr(it)
            },
            asyncAttr(ProfileConstants.arch) {
                (System.getProperty("os.arch") ?: "unknown").mapToAttr(it)
            },
            asyncAttr(ProfileConstants.arch_support) {
                Build.SUPPORTED_ABIS.joinToString().mapToAttr(it)
            }
        )
        attributes.awaitAll()
    }
}