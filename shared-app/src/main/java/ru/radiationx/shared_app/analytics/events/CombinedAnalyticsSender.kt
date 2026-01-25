package ru.radiationx.shared_app.analytics.events

import ru.radiationx.data.analytics.AnalyticsSender
import javax.inject.Inject

class CombinedAnalyticsSender @Inject constructor(
    private val appMetrica: AppMetricaAnalyticsSender,
    private val logging: LoggingAnalyticsSender,
) : AnalyticsSender {

    override fun send(key: String, vararg params: Pair<String, String>) {
        logging.send(key, *params)
        appMetrica.send(key, *params)
    }

    override fun error(groupId: String, message: String, throwable: Throwable) {
        logging.error(groupId, message, throwable)
        appMetrica.error(groupId, message, throwable)
    }
}