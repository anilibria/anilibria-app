package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import javax.inject.Inject

class CollectionsAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.collections_open,
            from.toNavFromParam()
        )
    }
}