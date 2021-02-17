package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class CatalogAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.catalog_open,
            "from" to from
        )
    }

    fun releaseClick() {
        sender.send(AnalyticsConstants.catalog_release_click)
    }

    fun fastSearchClick() {
        sender.send(AnalyticsConstants.catalog_fast_search_click)
    }

    fun filterClick() {
        sender.send(AnalyticsConstants.catalog_on_filter_click)
    }

    fun loadPage(page: Int) {
        sender.send(
            AnalyticsConstants.catalog_load_page,
            "page" to page.toString()
        )
    }

}