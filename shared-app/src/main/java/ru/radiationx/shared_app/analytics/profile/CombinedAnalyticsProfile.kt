package ru.radiationx.shared_app.analytics.profile

import ru.radiationx.data.analytics.profile.AnalyticsProfile
import javax.inject.Inject

class CombinedAnalyticsProfile @Inject constructor(
    private val appMetrica: AppMetricaAnalyticsProfile,
    private val logging: LoggingAnalyticsProfile
) : AnalyticsProfile {

    override fun update() {
        logging.update()
        appMetrica.update()
    }
}