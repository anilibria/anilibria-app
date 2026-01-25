package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import javax.inject.Inject

class UpdaterAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    private companion object {
        const val PARAM_SOURCE_TITLE = "title"
    }

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.updater_open,
            from.toNavFromParam()
        )
    }

    fun downloadClick() {
        sender.send(AnalyticsConstants.updater_download_click)
    }

    fun sourceDownload(sourceTitle: String) {
        sender.send(
            AnalyticsConstants.updater_source_download,
            sourceTitle.toParam(PARAM_SOURCE_TITLE)
        )
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.updater_use_time,
            timeInMillis.toTimeParam()
        )
    }

    fun appUpdateCardClick() {
        sender.send(AnalyticsConstants.app_update_card_click)
    }

    fun appUpdateCardCloseClick() {
        sender.send(AnalyticsConstants.app_update_card_close)
    }

}