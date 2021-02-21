package ru.radiationx.shared_app.analytics

import android.util.Log
import com.yandex.metrica.YandexMetrica
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class AppMetricaAnalyticsSender : AnalyticsSender {
    override fun send(key: String, vararg params: Pair<String, String>) {
        try {
            Log.d("AnalyticsSender", "key: $key, params: ${params.toMap()}")
            YandexMetrica.reportEvent(key, params.toMap())
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}