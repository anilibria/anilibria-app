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
            val rootCause = throwable.findRootCause()
            val groupName = "$groupId ${throwable::class.simpleName} ${rootCause.message}"
            YandexMetrica.reportError(groupName, message, throwable)
        } catch (e: Throwable) {
            Timber.e(e, "Error while sending error to appmetrica")
        }
    }

    private fun Throwable.findRootCause(): Throwable {
        var rootCause: Throwable? = this
        while (rootCause?.cause != null && rootCause.cause !== rootCause) {
            rootCause = rootCause.cause
        }
        return rootCause ?: this
    }
}