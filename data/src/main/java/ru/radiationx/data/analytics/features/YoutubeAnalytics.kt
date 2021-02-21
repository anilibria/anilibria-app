package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toIdParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toVidParam
import toothpick.InjectConstructor

@InjectConstructor
class YoutubeAnalytics(
    private val sender: AnalyticsSender
) {

    fun openVideo(from: String, id: Int, vid: String?) {
        sender.send(
            AnalyticsConstants.youtube_video_open,
            from.toNavFromParam(),
            id.toIdParam(),
            vid.toVidParam()
        )
    }

}