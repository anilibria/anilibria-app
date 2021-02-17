package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class AuthMainAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.auth_main_open,
            "from" to from
        )
    }

    fun socialClick(key: String) {
        sender.send(
            AnalyticsConstants.auth_main_social_click,
            "key" to key
        )
    }

    fun regClick() {
        sender.send(AnalyticsConstants.auth_main_reg_click)
    }

    fun regToSiteClick() {
        sender.send(AnalyticsConstants.auth_main_reg_to_site_click)
    }

    fun skipClick() {
        sender.send(AnalyticsConstants.auth_main_skip_click)
    }

    fun loginClick() {
        sender.send(AnalyticsConstants.auth_main_login_click)
    }

    fun error(throwable: Throwable) {
        sender.send(
            AnalyticsConstants.auth_main_error,
            "error" to throwable.toString()
        )
    }

    fun success() {
        sender.send(AnalyticsConstants.auth_main_success)
    }

    fun useTime(timeInMillis: Long) {
        sender.send(
            AnalyticsConstants.auth_main_use_time,
            "time" to timeInMillis.toString()
        )
    }

}