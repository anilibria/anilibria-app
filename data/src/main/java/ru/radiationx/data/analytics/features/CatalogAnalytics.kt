package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toPageParam
import javax.inject.Inject

class CatalogAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.catalog_open,
            from.toNavFromParam()
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
            page.toPageParam()
        )
    }

}