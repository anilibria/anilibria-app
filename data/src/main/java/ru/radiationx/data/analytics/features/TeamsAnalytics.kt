package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import toothpick.InjectConstructor

@InjectConstructor
class TeamsAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.teams_open,
            from.toNavFromParam()
        )
    }

    fun joinClick() {
        sender.send(AnalyticsConstants.teams_join_click)
    }

}