package ru.radiationx.data.analytics

import android.util.Log
import toothpick.InjectConstructor

@InjectConstructor
class LoggingAnalyticsSender : AnalyticsSender {

    override fun send(key: String, vararg params: Pair<String, String>) {
        Log.d("AnalyticsSender", "key: $key, params: ${params.toMap()}")
    }
}