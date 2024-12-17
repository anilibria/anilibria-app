package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toErrorParam
import ru.radiationx.data.analytics.features.extensions.toIdParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import javax.inject.Inject

class WebPlayerAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.web_player_open,
            from.toNavFromParam(),
            releaseId.toIdParam()
        )
    }

    fun loaded() {
        sender.send(AnalyticsConstants.web_player_loaded)
    }

    fun error() {
        sender.send(AnalyticsConstants.web_player_error)
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.web_player_use_time,
            timeInMillis.toTimeParam()
        )
    }
}