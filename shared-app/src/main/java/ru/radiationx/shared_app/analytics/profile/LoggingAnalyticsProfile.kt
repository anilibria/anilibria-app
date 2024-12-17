package ru.radiationx.shared_app.analytics.profile

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.radiationx.data.analytics.profile.AnalyticsInstallerDataSource
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.analytics.profile.AnalyticsProfileDataSource
import ru.radiationx.data.analytics.profile.ProfileAttribute
import ru.radiationx.shared_app.analytics.CodecsProfileAnalytics
import timber.log.Timber
import javax.inject.Inject

class LoggingAnalyticsProfile @Inject constructor(
    private val info: AnalyticsProfileDataSource,
    private val codecs: CodecsProfileAnalytics,
    private val installer: AnalyticsInstallerDataSource
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
            val infoAttributes = info.getAttributes()
            val codecAttributes = codecs.getAttributes()
            val installerAttributes = installer.getAttributes()
            val allAttributes = infoAttributes + codecAttributes + installerAttributes
            allAttributes.forEach {
                when (it) {
                    is ProfileAttribute.Boolean,
                    is ProfileAttribute.String,
                    is ProfileAttribute.Number -> {
                        Timber.tag("LoggingAnalyticsProfile").d(it.toString())
                    }

                    is ProfileAttribute.Error -> {
                        Timber.tag("LoggingAnalyticsProfile").e(it.toString())
                    }
                }
            }
        }
    }
}