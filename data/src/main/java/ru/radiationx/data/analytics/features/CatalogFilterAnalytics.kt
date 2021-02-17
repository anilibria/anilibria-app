package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class CatalogFilterAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.catalog_filter_open,
            "from" to from
        )
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.catalog_filter_use_time,
            "time" to timeInMillis.toString()
        )
    }

    fun applyClick() {
        sender.send(AnalyticsConstants.catalog_filter_apply_click)
    }

}