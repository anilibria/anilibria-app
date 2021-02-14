package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class FeedScheduleAnalytics(
    private val sender: AnalyticsSender
) {

    fun navSchedule() {
        sender.send(AnalyticsConstants.feed_schedule_nav_schedule)
    }

    fun navRelease(releaseId: Int) {
        sender.send(AnalyticsConstants.feed_schedule_nav_release, "id" to releaseId.toString())
    }
}