package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toPositionParam
import toothpick.InjectConstructor

@InjectConstructor
class ScheduleAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.schedule_open,
            from.toNavFromParam()
        )
    }

    fun horizontalScroll(position: Int) {
        sender.send(
            AnalyticsConstants.schedule_horizontal_scroll,
            position.toPositionParam()
        )
    }

    fun releaseClick(position: Int) {
        sender.send(
            AnalyticsConstants.schedule_release_click,
            position.toPositionParam()
        )
    }
}