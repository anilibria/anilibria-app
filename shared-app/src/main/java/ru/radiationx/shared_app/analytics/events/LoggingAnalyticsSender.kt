package ru.radiationx.shared_app.analytics.events

import ru.radiationx.data.analytics.AnalyticsSender
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class LoggingAnalyticsSender : AnalyticsSender {

    override fun send(key: String, vararg params: Pair<String, String>) {
        Timber.tag("AnalyticsSender").d("key: $key, params: ${params.toMap()}")
    }

    override fun error(groupId: String, message: String, throwable: Throwable) {
        Timber.tag("AnalyticsSender").e(throwable, "groupId: $groupId, message: $message")
    }
}