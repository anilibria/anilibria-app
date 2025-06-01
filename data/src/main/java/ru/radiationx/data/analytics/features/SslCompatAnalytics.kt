package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.network.sslcompat.SslCompat
import javax.inject.Inject

class SslCompatAnalytics @Inject constructor(
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