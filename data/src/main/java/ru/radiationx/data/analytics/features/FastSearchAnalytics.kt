package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import toothpick.InjectConstructor

@InjectConstructor
class FastSearchAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.fast_search_open,
            from.toNavFromParam()
        )
    }

    fun releaseClick() {
        sender.send(AnalyticsConstants.fast_search_release_click)
    }

    fun catalogClick() {
        sender.send(AnalyticsConstants.fast_search_catalog_click)
    }

    fun searchGoogleClick() {
        sender.send(AnalyticsConstants.fast_search_google_click)
    }
}