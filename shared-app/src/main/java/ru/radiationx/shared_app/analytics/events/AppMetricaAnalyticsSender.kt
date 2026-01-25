package ru.radiationx.shared_app.analytics.events

import io.appmetrica.analytics.AppMetrica
import ru.radiationx.data.analytics.AnalyticsSender
import timber.log.Timber
import javax.inject.Inject

class AppMetricaAnalyticsSender @Inject constructor() : AnalyticsSender {
    override fun send(key: String, vararg params: Pair<String, String>) {
        try {
            if (params.isEmpty()) {
                AppMetrica.reportEvent(key)
            } else {
                AppMetrica.reportEvent(key, params.toMap())
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error while sending event to appmetrica")
        }
    }

    override fun error(groupId: String, message: String, throwable: Throwable) {
        try {
            AppMetrica.reportError(groupId, message, throwable)
        } catch (e: Throwable) {
            Timber.e(e, "Error while sending error to appmetrica")
        }
    }
}