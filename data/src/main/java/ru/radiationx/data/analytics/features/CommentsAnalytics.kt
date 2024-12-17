package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toIdParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import javax.inject.Inject

class CommentsAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.comments_open,
            from.toNavFromParam(),
            releaseId.toIdParam()
        )
    }

    fun loaded() {
        sender.send(AnalyticsConstants.comments_loaded)
    }

    fun error() {
        sender.send(AnalyticsConstants.comments_error)
    }

}