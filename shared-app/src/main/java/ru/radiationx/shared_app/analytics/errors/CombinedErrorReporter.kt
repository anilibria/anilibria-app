package ru.radiationx.shared_app.analytics.errors

import ru.radiationx.data.analytics.AnalyticsErrorReporter
import javax.inject.Inject

class CombinedErrorReporter @Inject constructor(
    private val appMetrica: AppMetricaErrorReporter,
    private val logging: LoggingErrorReporter
) : AnalyticsErrorReporter {

    override fun report(message: String, error: Throwable) {
        logging.report(message, error)
        appMetrica.report(message, error)
    }

    override fun report(group: String, message: String) {
        logging.report(group, message)
        appMetrica.report(group, message)
    }

    override fun report(group: String, message: String, error: Throwable) {
        logging.report(group, message, error)
        appMetrica.report(group, message, error)
    }
}