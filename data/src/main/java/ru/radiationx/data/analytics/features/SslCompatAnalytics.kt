package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.sslcompat.SslCompat
import toothpick.InjectConstructor

@InjectConstructor
class SslCompatAnalytics(
    private val sender: AnalyticsSender,
) {

    private var sentError: Throwable? = null

    fun oneShotError(data: Result<SslCompat.Data>) {
        val error = data.exceptionOrNull() ?: return
        if (error == sentError) return
        sentError = error
        sender.error("sslCompatError", error.message.orEmpty(), error)
    }

}