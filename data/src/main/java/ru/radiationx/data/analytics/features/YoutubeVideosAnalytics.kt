package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class YoutubeVideosAnalytics(
    private val sender: AnalyticsSender
) {

    fun openVideos(from: String) {
        sender.send(
            AnalyticsConstants.youtube_videos_open,
            "from" to from
        )
    }

    fun videoClick() {
        sender.send(AnalyticsConstants.youtube_videos_video_click)
    }

    fun loadPage(page: Int) {
        sender.send(
            AnalyticsConstants.youtube_videos_load_page,
            "page" to page.toString()
        )
    }

}