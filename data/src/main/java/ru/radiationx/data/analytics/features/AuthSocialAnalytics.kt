package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import javax.inject.Inject

class AuthSocialAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String, key: String) {
        sender.send(
            AnalyticsConstants.auth_social_open,
            from.toNavFromParam(),
            key.toKeyParam()
        )
    }

    fun error(key: String) {
        sender.send(
            AnalyticsConstants.auth_social_error,
            key.toKeyParam()
        )
    }

    fun pageError(key: String) {
        sender.send(
            AnalyticsConstants.auth_social_page_error,
            key.toKeyParam()
        )
    }


    fun success(key: String) {
        sender.send(
            AnalyticsConstants.auth_social_success,
            key.toKeyParam()
        )
    }

    fun useTime(key: String, timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.auth_social_use_time,
            key.toKeyParam(),
            timeInMillis.toTimeParam()
        )
    }

    private fun String.toKeyParam() = toParam("key")

}