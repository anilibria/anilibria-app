package ru.radiationx.shared_app.analytics.events

import com.yandex.metrica.YandexMetrica
import ru.radiationx.data.analytics.AnalyticsSender
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class AppMetricaAnalyticsSender : AnalyticsSender {
    override fun send(key: String, vararg params: Pair<String, String>) {
        try {
            if (params.isEmpty()) {
                YandexMetrica.reportEvent(key)
            } else {
                YandexMetrica.reportEvent(key, params.toMap())
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error while sending event to appmetrica")
        }
    }

    override fun error(groupId: String, message: String, throwable: Throwable) {
        try {
            YandexMetrica.reportError(groupId, message, throwable)
        } catch (e: Throwable) {
            Timber.e(e, "Error while sending error to appmetrica")
        }
    }
}