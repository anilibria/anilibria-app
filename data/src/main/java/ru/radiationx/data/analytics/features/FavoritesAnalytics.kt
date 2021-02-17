package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class FavoritesAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.favorites_open,
            "from" to from
        )
    }

    fun searchClick() {
        sender.send(AnalyticsConstants.favorites_search_click)
    }

    fun searchReleaseClick() {
        sender.send(AnalyticsConstants.favorites_search_release_click)
    }

    fun releaseClick() {
        sender.send(AnalyticsConstants.favorites_release_click)
    }

    fun loadPage(page: Int) {
        sender.send(
            AnalyticsConstants.favorites_load_page,
            "page" to page.toString()
        )
    }

}