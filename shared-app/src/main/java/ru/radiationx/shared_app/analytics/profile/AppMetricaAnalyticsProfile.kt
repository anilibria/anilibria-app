package ru.radiationx.shared_app.analytics.profile

import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.profile.Attribute
import io.appmetrica.analytics.profile.UserProfile
import io.appmetrica.analytics.profile.UserProfileUpdate
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.radiationx.data.analytics.profile.AnalyticsInstallerProfileDataSource
import ru.radiationx.data.analytics.profile.AnalyticsMainProfileDataSource
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.analytics.profile.ProfileAttribute
import ru.radiationx.data.analytics.profile.ProfileConstants
import ru.radiationx.shared_app.analytics.AnalyticsCodecsProfileDataSource
import timber.log.Timber
import javax.inject.Inject

class AppMetricaAnalyticsProfile @Inject constructor(
    private val main: AnalyticsMainProfileDataSource,
    private val codecs: AnalyticsCodecsProfileDataSource,
    private val installer: AnalyticsInstallerProfileDataSource
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
        GlobalScope.launch {
            val mainAttributes = main.getAttributes()
            val codecAttributes = codecs.getAttributes()
            val installerAttributes = installer.getAttributes()
            val allAttributes = mainAttributes + codecAttributes + installerAttributes
            val yandexAttributes = allAttributes.mapNotNull { it.mapToYandex() }
            val errorAttributes = allAttributes.filterIsInstance<ProfileAttribute.Error>()
            val userProfile = UserProfile.newBuilder().run {
                yandexAttributes.forEach { attribute ->
                    apply(attribute)
                }
                apply(errorAttributes.mapToYandexFail())
                build()
            }
            AppMetrica.reportUserProfile(userProfile)
        }
    }

    private fun ProfileAttribute.mapToYandex(): UserProfileUpdate<*>? = when (this) {
        is ProfileAttribute.String -> Attribute.customString(name).withValue(value)
        is ProfileAttribute.Number -> Attribute.customNumber(name).withValue(value.toDouble())
        is ProfileAttribute.Boolean -> Attribute.customBoolean(name).withValue(value)
        is ProfileAttribute.Error -> null
    }

    private fun List<ProfileAttribute.Error>.mapToYandexFail(): UserProfileUpdate<*> {
        val names = map { it.name }
        val attribute = Attribute.customString(ProfileConstants.fail_attributes)
        return if (names.isEmpty()) {
            attribute.withValueReset()
        } else {
            attribute.withValue(names.joinToString())
        }
    }
}