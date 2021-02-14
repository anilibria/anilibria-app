package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class FastSearchAnalytics(
    private val sender: AnalyticsSender
) {

    fun open() {
        sender.send(AnalyticsConstants.fsearch_open)
    }

    fun cancel() {
        sender.send(AnalyticsConstants.fsearch_cancel)
    }

    fun navRelease(releaseId: Int) {
        sender.send(
            AnalyticsConstants.fsearch_nav_release,
            "id" to releaseId.toString()
        )
    }

    fun navCatalog() {
        sender.send(AnalyticsConstants.fsearch_nav_catalog)
    }

    fun navGoogle() {
        sender.send(AnalyticsConstants.fsearch_nav_google)
    }
}