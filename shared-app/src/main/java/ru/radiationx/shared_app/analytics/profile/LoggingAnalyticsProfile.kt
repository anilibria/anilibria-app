package ru.radiationx.shared_app.analytics.profile

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.analytics.profile.AnalyticsProfileDataSource
import ru.radiationx.data.analytics.profile.ProfileConstants
import ru.radiationx.shared_app.analytics.CodecsProfileAnalytics
import timber.log.Timber
import javax.inject.Inject

class LoggingAnalyticsProfile @Inject constructor(
    private val dataSource: AnalyticsProfileDataSource,
    private val codecs: CodecsProfileAnalytics,
) : AnalyticsProfile {

    override fun update() {
        try {
            unsafeUpdate()
        } catch (ex: Throwable) {
            Timber.e(ex)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun unsafeUpdate() {
        val singleSources = with(dataSource) {
            listOf(
                getApiAddressTag().mapToAttr(ProfileConstants.address_tag),
                getAppTheme().mapToAttr(ProfileConstants.app_theme),
                getQualitySettings().mapToAttr(ProfileConstants.quality),
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
                    Timber.tag("LoggingAnalyticsProfile").d(it.toMap().toString())
                }
                .launchIn(GlobalScope)
        }
    }

    private fun Flow<Any>.mapToAttr(name: String): Flow<Pair<String, Any>> = map {
        Pair(name, it)
    }
}