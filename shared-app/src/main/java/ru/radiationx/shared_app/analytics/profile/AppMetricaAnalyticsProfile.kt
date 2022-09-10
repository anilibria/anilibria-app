package ru.radiationx.shared_app.analytics.profile

import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.profile.Attribute
import com.yandex.metrica.profile.UserProfile
import com.yandex.metrica.profile.UserProfileUpdate
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
class AppMetricaAnalyticsProfile(
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
            listOf<Single<DataWrapper<UserProfileUpdate<*>>>>(
                getApiAddressTag().mapStringAttr(ProfileConstants.address_tag),
                getAppTheme().mapStringAttr(ProfileConstants.app_theme),
                getQualitySettings().mapStringAttr(ProfileConstants.quality),
                getPlayerSettings().mapStringAttr(ProfileConstants.player),
                getPipSettings().mapStringAttr(ProfileConstants.pip),
                getPlaySpeedSettings().mapFloatAttr(ProfileConstants.play_speed),
                getNotificationsAllSettings().mapBoolAttr(ProfileConstants.notification_all),
                getNotificationsServiceSettings().mapBoolAttr(ProfileConstants.notification_service),
                getEpisodeOrderSettings().mapBoolAttr(ProfileConstants.episode_order),
                getAuthState().mapStringAttr(ProfileConstants.auth_state),
                getHistoryItemsCount().mapIntAttr(ProfileConstants.history_count),
                getEpisodesItemsCount().mapIntAttr(ProfileConstants.episodes_count),
                getReleasesItemsCount().mapIntAttr(ProfileConstants.releases_count),
                getDownloadsCount().mapIntAttr(ProfileConstants.downloads_count),
                getAppVersionsHistory().mapStringAttr(ProfileConstants.app_versions)
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
                    .map { codecsMap ->
                        codecsMap.toList().map {
                            Attribute.customString(it.first).withValue(it.second)
                        }
                    }
                    .map { mainParams + it }
            }
            .map { attributes ->
                UserProfile.newBuilder().run {
                    attributes.forEach { attribute ->
                        apply(attribute)
                    }
                    build()
                }
            }
            .subscribe({
                YandexMetrica.reportUserProfile(it)
            }, {
                it.printStackTrace()
            })
    }

    private fun Single<String>.mapStringAttr(name: String) = this
        .attachScheduler()
        .map { Attribute.customString(name).withValue(it).toWrapper() }
        .nullOnError()

    private fun Single<Int>.mapIntAttr(name: String) = this
        .attachScheduler()
        .map { Attribute.customNumber(name).withValue(it.toDouble()).toWrapper() }
        .nullOnError()

    private fun Single<Float>.mapFloatAttr(name: String) = this
        .attachScheduler()
        .map { Attribute.customNumber(name).withValue(it.toDouble()).toWrapper() }
        .nullOnError()

    private fun Single<Boolean>.mapBoolAttr(name: String) = this
        .attachScheduler()
        .map { Attribute.customBoolean(name).withValue(it).toWrapper() }
        .nullOnError()

    private fun <T> Single<T>.attachScheduler() = this.subscribeOn(Schedulers.io())
}