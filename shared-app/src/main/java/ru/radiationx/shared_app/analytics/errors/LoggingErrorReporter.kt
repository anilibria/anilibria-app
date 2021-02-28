package ru.radiationx.shared_app.analytics.errors

import android.util.Log
import com.yandex.metrica.YandexMetrica
import ru.radiationx.data.analytics.AnalyticsErrorReporter
import toothpick.InjectConstructor

@InjectConstructor
class LoggingErrorReporter : AnalyticsErrorReporter {

    override fun report(message: String, error: Throwable) {
        Log.e("LoggingErrorReporter", message, error)
    }

    override fun report(group: String, message: String) {
        Log.e("LoggingErrorReporter", "$group -> $message")
    }

    override fun report(group: String, message: String, error: Throwable) {
        Log.e("LoggingErrorReporter", "$group -> $message", error)
    }
}