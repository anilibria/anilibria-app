package ru.radiationx.shared_app.analytics

import android.util.Log
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class LoggingAnalyticsSender : AnalyticsSender {

    override fun send(key: String, vararg params: Pair<String, String>) {
        Log.d("AnalyticsSender", "key: $key, params: ${params.toMap()}")
    }
}