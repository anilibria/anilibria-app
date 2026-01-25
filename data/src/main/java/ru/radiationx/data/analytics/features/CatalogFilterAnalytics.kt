package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import javax.inject.Inject

class CatalogFilterAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.catalog_filter_open,
            from.toNavFromParam()
        )
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.catalog_filter_use_time,
            timeInMillis.toTimeParam()
        )
    }

    fun applyClick() {
        sender.send(AnalyticsConstants.catalog_filter_apply_click)
    }

}