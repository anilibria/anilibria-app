package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toErrorParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import toothpick.InjectConstructor

@InjectConstructor
class WebPlayerAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.web_player_open,
            from.toNavFromParam()
        )
    }

    fun loaded() {
        sender.send(AnalyticsConstants.web_player_loaded)
    }

    fun error(error: Throwable) {
        sender.send(
            AnalyticsConstants.web_player_error,
            error.toErrorParam()
        )
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.web_player_use_time,
            timeInMillis.toTimeParam()
        )
    }
}