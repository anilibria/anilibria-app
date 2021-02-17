package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class YoutubeAnalytics(
    private val sender: AnalyticsSender
) {

    fun openVideo(from: String, id: Int, vid: String) {
        sender.send(
            AnalyticsConstants.youtube_video_open,
            "from" to from,
            "id" to id.toString(),
            "vid" to vid
        )
    }

}