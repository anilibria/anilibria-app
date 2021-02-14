package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class FastSearchAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.fsearch_open,
            "from" to from
        )
    }

    fun cancel(from: String) {
        sender.send(
            AnalyticsConstants.fsearch_cancel,
            "from" to from
        )
    }

    fun catalogClick() {
        sender.send(AnalyticsConstants.fsearch_catalog_click)
    }

    fun searchGoogleClick() {
        sender.send(AnalyticsConstants.fsearch_google_click)
    }
}