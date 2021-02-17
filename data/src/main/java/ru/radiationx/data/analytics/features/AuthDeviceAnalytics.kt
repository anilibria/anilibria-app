package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class AuthDeviceAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.auth_device_open,
            "from" to from
        )
    }

    fun error(throwable: Throwable) {
        sender.send(
            AnalyticsConstants.auth_device_error,
            "error" to throwable.toString()
        )
    }

    fun success() {
        sender.send(AnalyticsConstants.auth_device_success)
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.auth_device_use_time,
            "time" to timeInMillis.toString()
        )
    }

}