package ru.radiationx.shared_app.analytics.profile

import android.util.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.analytics.profile.AnalyticsProfileDataSource
import ru.radiationx.data.analytics.profile.ProfileConstants
import ru.radiationx.data.entity.common.DataWrapper
import ru.radiationx.data.extensions.nullOnError
import ru.radiationx.data.extensions.toWrapper
import ru.radiationx.shared_app.analytics.CodecsProfileAnalytics
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
            ex.printStackTrace()
        }
    }

    private fun unsafeUpdate() {
        val singleSources = with(dataSource) {
            listOf<Single<DataWrapper<Pair<String, Any>>>>(
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
        val ignoreDisposable = Single
            .merge(singleSources)
            .filter { it.data != null }
            .map { it.data!! }
            .toList()
            .flatMap { mainParams ->
                codecs
                    .getCodecsInfo()
                    .map { mainParams + it.toList() }
            }
            .subscribe({
                Log.d("LoggingAnalyticsProfile", it.toMap().toString())
            }, {
                it.printStackTrace()
            })
    }

    private fun Single<out Any>.mapToAttr(name: String): Single<DataWrapper<Pair<String, Any>>> =
        this
            .attachScheduler()
            .map { Pair(name, it).toWrapper() }
            .nullOnError()

    private fun <T> Single<T>.attachScheduler() = this.subscribeOn(Schedulers.io())
}