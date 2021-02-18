package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import toothpick.InjectConstructor

@InjectConstructor
class CommentsAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.comments_open,
            from.toNavFromParam()
        )
    }

    fun loaded() {
        sender.send(AnalyticsConstants.comments_loaded)
    }

    fun error() {
        sender.send(AnalyticsConstants.comments_error)
    }

}