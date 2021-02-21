package ru.radiationx.shared_app.analytics

import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class CombinedAnalyticsSender(
    private val appMetrica: AppMetricaAnalyticsSender,
    private val logging: LoggingAnalyticsSender
) : AnalyticsSender {

    override fun send(key: String, vararg params: Pair<String, String>) {
        logging.send(key, *params)
        appMetrica.send(key, *params)
    }
}