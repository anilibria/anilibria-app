package ru.radiationx.shared_app.analytics.profile

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.analytics.profile.AnalyticsProfileDataSource
import ru.radiationx.data.analytics.profile.ProfileConstants
import ru.radiationx.shared_app.analytics.CodecsProfileAnalytics
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class LoggingAnalyticsProfile(
    private val dataSource: AnalyticsProfileDataSource,
    private val codecs: CodecsProfileAnalytics
) : AnalyticsProfile {

    override fun update() {
        try {
            unsafeUpdate()
        } catch (ex: Throwable) {
            Timber.e(ex)
        }
    }

    private fun unsafeUpdate() {
        val singleSources = with(dataSource) {
            listOf<Flow<Pair<String, Any>>>(
                getApiAddressTag().mapToAttr(ProfileConstants.address_tag),
                getAppTheme().mapToAttr(ProfileConstants.app_theme),
                getQualitySettings().mapToAttr(ProfileConstants.quality),
                getPlayerSettings().mapToAttr(ProfileConstants.player),
                getPipSettings().mapToAttr(ProfileConstants.pip),
                getPlaySpeedSettings().mapToAttr(ProfileConstants.play_speed),
                getNotificationsAllSettings().mapToAttr(ProfileConstants.notification_all),
                getNotificationsServiceSettings().mapToAttr(ProfileConstants.notification_service),
                getEpisodeOrderSettings().mapToAttr(ProfileConstants.episode_order),
                getAuthState().mapToAttr(ProfileConstants.auth_state),
                getHistoryItemsCount().mapToAttr(ProfileConstants.history_count),
                getEpisodesItemsCount().mapToAttr(ProfileConstants.episodes_count),
                getReleasesItemsCount().mapToAttr(ProfileConstants.releases_count),
                getDownloadsCount().mapToAttr(ProfileConstants.downloads_count),
                getAppVersionsHistory().mapToAttr(ProfileConstants.app_versions)
            )
        }

        GlobalScope.launch {
            flow {
                emit(merge(*singleSources.toTypedArray()).toList())
            }
                .map { mainParams ->
                    codecs
                        .getCodecsInfo()
                        .let { mainParams + it.toList() }
                }
                .catch {
                    Timber.e(it)
                }
                .onEach {
                    Log.d("LoggingAnalyticsProfile", it.toMap().toString())
                }
                .launchIn(GlobalScope)
        }
    }

    private fun Flow<Any>.mapToAttr(name: String): Flow<Pair<String, Any>> = map {
        Pair(name, it)
    }
}