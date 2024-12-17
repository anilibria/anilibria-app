package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import javax.inject.Inject

class PageAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    private companion object {
        const val PARAM_PATH = "path"
    }

    fun open(from: String, path: String) {
        sender.send(
            AnalyticsConstants.page_open,
            from.toNavFromParam(),
            path.toParam(PARAM_PATH)
        )
    }

    fun loaded() {
        sender.send(AnalyticsConstants.page_loaded)
    }

    fun error() {
        sender.send(AnalyticsConstants.page_error)
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.page_use_time,
            timeInMillis.toTimeParam()
        )
    }
}