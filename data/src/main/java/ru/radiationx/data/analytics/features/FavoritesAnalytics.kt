package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toPageParam
import toothpick.InjectConstructor

@InjectConstructor
class FavoritesAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.favorites_open,
            from.toNavFromParam()
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

    fun deleteFav() {
        sender.send(AnalyticsConstants.favorites_delete_click)
    }

    fun loadPage(page: Int) {
        sender.send(
            AnalyticsConstants.favorites_load_page,
            page.toPageParam()
        )
    }

}