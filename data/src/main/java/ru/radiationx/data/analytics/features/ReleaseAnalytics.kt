package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class ReleaseAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(releaseId: Int, from: String) {
        sender.send(
            AnalyticsConstants.release_open,
            "from" to from,
            "id" to releaseId.toString()
        )
    }

    fun copyLink(from: String) {
        sender.send(
            AnalyticsConstants.release_copy,
            "from" to from
        )
    }

    fun share(from: String) {
        sender.send(
            AnalyticsConstants.release_share,
            "from" to from
        )
    }

    fun shortcut(from: String) {
        sender.send(
            AnalyticsConstants.release_shortcut,
            "from" to from
        )
    }
}