package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class AuthSocialAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.auth_social_open,
            "from" to from
        )
    }

    fun error(throwable: Throwable) {
        sender.send(
            AnalyticsConstants.auth_social_error,
            "error" to throwable.toString()
        )
    }

    fun success() {
        sender.send(AnalyticsConstants.auth_social_success)
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.auth_social_use_time,
            "time" to timeInMillis.toString()
        )
    }

}