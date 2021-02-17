package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class ScheduleAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.schedule_open,
            "from" to from
        )
    }

    fun horizontalScroll(position: Int) {
        sender.send(
            AnalyticsConstants.schedule_horizontal_scroll,
            "position" to position.toString()
        )
    }

    fun releaseClick(position: Int) {
        sender.send(
            AnalyticsConstants.schedule_release_click,
            "position" to position.toString()
        )
    }
}