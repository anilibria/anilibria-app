package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class HistoryAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.history_open,
            "from" to from
        )
    }

    fun searchClick() {
        sender.send(AnalyticsConstants.history_search_click)
    }

    fun searchReleaseClick() {
        sender.send(AnalyticsConstants.history_search_release_click)
    }

    fun releaseClick() {
        sender.send(AnalyticsConstants.history_release_click)
    }

}