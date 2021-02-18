package ru.radiationx.shared_app.analytics

import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.profile.Attribute
import com.yandex.metrica.profile.StringAttribute
import com.yandex.metrica.profile.UserProfile
import com.yandex.metrica.profile.UserProfileUpdate
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.analytics.profile.AnalyticsProfileDataSource
import toothpick.InjectConstructor

@InjectConstructor
class AppmetricaAnalyticsProfile(
    private val dataSource: AnalyticsProfileDataSource
) : AnalyticsProfile {

    override fun update() {
        val singleSources = with(dataSource) {
            listOf<Single<DataWrapper<UserProfileUpdate<*>>>>(
                getApiAddressTag().mapStringAttr(""),
                getAppTheme().mapStringAttr(""),
                getQualitySettings().mapStringAttr(""),
                getPlayerSettings().mapStringAttr(""),
                getPipSettings().mapStringAttr(""),
                getPlaySpeedSettings().mapFloatAttr(""),
                getNotificationsAllSettings().mapBoolAttr(""),
                getNotificationsServiceSettings().mapBoolAttr(""),
                getEpisodeOrderSettings().mapBoolAttr(""),
                getAuthState().mapStringAttr(""),
                getHistoryItemsCount().mapIntAttr(""),
                getEpisodesItemsCount().mapIntAttr(""),
                getDownloadsCount().mapIntAttr("")
            )
        }

        val ignoreDisposable = Single
            .merge(singleSources)
            .filter { it.data != null }
            .map { it.data!! }
            .toList()
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

    private fun <T> Single<DataWrapper<T>>.nullOnError() =
        this.onErrorReturn { DataWrapper(null) }

    private fun <T> T.toWrapper() = DataWrapper(this)

    private class DataWrapper<T>(val data: T?)
}