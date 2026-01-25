package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toPageParam
import javax.inject.Inject

class YoutubeVideosAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.youtube_videos_open,
            from.toNavFromParam()
        )
    }

    fun videoClick() {
        sender.send(AnalyticsConstants.youtube_videos_video_click)
    }

    fun loadPage(page: Int) {
        sender.send(
            AnalyticsConstants.youtube_videos_load_page,
            page.toPageParam()
        )
    }

}