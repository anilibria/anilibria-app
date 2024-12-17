package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import javax.inject.Inject

class AuthDeviceAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.auth_device_open,
            from.toNavFromParam()
        )
    }

    fun error() {
        sender.send(AnalyticsConstants.auth_device_error)
    }

    fun success() {
        sender.send(AnalyticsConstants.auth_device_success)
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.auth_device_use_time,
            timeInMillis.toTimeParam()
        )
    }

}