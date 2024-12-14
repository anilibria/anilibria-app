package ru.radiationx.shared_app.analytics.profile

import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.profile.Attribute
import io.appmetrica.analytics.profile.UserProfile
import io.appmetrica.analytics.profile.UserProfileUpdate
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
            Timber.e(ex)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun unsafeUpdate() {
        val singleSources = with(dataSource) {
            listOf<Flow<UserProfileUpdate<*>>>(
                getApiAddressTag().mapStringAttr(ProfileConstants.address_tag),
                getAppTheme().mapStringAttr(ProfileConstants.app_theme),
                getQualitySettings().mapStringAttr(ProfileConstants.quality),
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

        GlobalScope.launch {
            flow {
                emit(merge(*singleSources.toTypedArray()).toList())
            }
                .map { mainParams ->
                    codecs
                        .getCodecsInfo()
                        .let { codecsMap ->
                            codecsMap.toList().map {
                                Attribute.customString(it.first).withValue(it.second)
                            }
                        }
                        .let { mainParams + it }
                }
                .map { attributes ->
                    UserProfile.newBuilder().run {
                        attributes.forEach { attribute ->
                            apply(attribute)
                        }
                        build()
                    }
                }
                .catch {
                    Timber.e(it)
                }
                .onEach {
                    AppMetrica.reportUserProfile(it)
                }
                .launchIn(GlobalScope)
        }
    }

    private fun Flow<String>.mapStringAttr(name: String) = this
        .map { Attribute.customString(name).withValue(it) }

    private fun Flow<Int>.mapIntAttr(name: String) = this
        .map { Attribute.customNumber(name).withValue(it.toDouble()) }

    private fun Flow<Float>.mapFloatAttr(name: String) = this
        .map { Attribute.customNumber(name).withValue(it.toDouble()) }

    private fun Flow<Boolean>.mapBoolAttr(name: String) = this
        .map { Attribute.customBoolean(name).withValue(it) }

}