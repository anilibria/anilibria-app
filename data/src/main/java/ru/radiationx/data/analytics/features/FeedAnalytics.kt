package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toPageParam
import ru.radiationx.data.analytics.features.extensions.toPositionParam
import javax.inject.Inject

class FeedAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.feed_open,
            from.toNavFromParam()
        )
    }

    fun loadPage(page: Int) {
        sender.send(
            AnalyticsConstants.feed_load_page,
            page.toPageParam()
        )
    }

    fun scheduleClick() {
        sender.send(AnalyticsConstants.feed_schedule_click)
    }

    fun scheduleHorizontalScroll(position: Int) {
        sender.send(
            AnalyticsConstants.feed_schedule_horizontal_scroll,
            position.toPositionParam()
        )
    }

    fun scheduleReleaseClick(position: Int) {
        sender.send(
            AnalyticsConstants.feed_schedule_release_click,
            position.toPositionParam()
        )
    }

    fun releaseClick() {
        sender.send(AnalyticsConstants.feed_release_click)
    }

    fun youtubeClick() {
        sender.send(AnalyticsConstants.feed_youtube_click)
    }

    fun randomClick() {
        sender.send(AnalyticsConstants.feed_random_click)
    }
}