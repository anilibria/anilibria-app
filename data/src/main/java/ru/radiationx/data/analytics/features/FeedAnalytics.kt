package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class FeedAnalytics(
    private val sender: AnalyticsSender
) {

    fun onRandomClick() {
        sender.send(AnalyticsConstants.feed_random_click)
    }

    fun navRelease(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_open,
            "from" to AnalyticsConstants.screen_feed,
            "id" to releaseId.toString()
        )
    }

    fun navYoutube(youtubeId: Int, vid: String) {
        sender.send(
            AnalyticsConstants.youtube_video_open,
            "from" to AnalyticsConstants.screen_feed,
            "id" to youtubeId.toString(),
            "vid" to vid
        )
    }

    fun loadPage(page: Int) {
        sender.send(
            AnalyticsConstants.feed_load_page,
            "page" to page.toString()
        )
    }
}