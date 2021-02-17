package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.model.AnalyticsConfigState
import toothpick.InjectConstructor

@InjectConstructor
class ConfiguringAnalytics(
    private val sender: AnalyticsSender
) {

    private fun Long.toTimeParam() = Pair("time", this.toString())
    private fun Boolean.toSuccessParam() = Pair("success", this.toString())
    private fun String?.toAddressParam() = Pair("address", this.toString())
    private fun Throwable?.toErrorParam() = Pair("error", this.toString())
    private fun AnalyticsConfigState.toStateParam() = Pair("state", this.toString())

    private fun <T> T?.asParam(name: String) = Pair<String, String>(name, this.toString())

    fun open() {
        sender.send(AnalyticsConstants.config_open)
    }

    fun checkFull(
        startAddressTag: String,
        endAddressTag: String,
        timeInMillis: Long,
        isSuccess: Boolean
    ) {
        sender.send(
            AnalyticsConstants.config_check_full,
            "startAddress" to startAddressTag,
            "endAddressTag" to endAddressTag,
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam()
        )
    }


    fun checkLast(
        addressTag: String,
        timeInMillis: Long,
        isSuccess: Boolean,
        error: Throwable? = null
    ) {
        sender.send(
            AnalyticsConstants.config_check_last,
            addressTag.toAddressParam(),
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam(),
            error.toErrorParam()
        )
    }

    fun loadConfig(
        timeInMillis: Long,
        isSuccess: Boolean,
        error: Throwable? = null
    ) {
        sender.send(
            AnalyticsConstants.config_load_config,
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam(),
            error.toErrorParam()
        )
    }

    fun checkAvail(
        addressTag: String?,
        timeInMillis: Long,
        isSuccess: Boolean,
        error: Throwable? = null
    ) {
        sender.send(
            AnalyticsConstants.config_check_avail,
            addressTag.toAddressParam(),
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam(),
            error.toErrorParam()
        )
    }

    fun checkProxies(
        addressTag: String?,
        timeInMillis: Long,
        isSuccess: Boolean,
        error: Throwable? = null
    ) {
        sender.send(
            AnalyticsConstants.config_check_proxies,
            addressTag.toAddressParam(),
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam(),
            error.toErrorParam()
        )
    }


    fun onRepeatClick(state: AnalyticsConfigState) {
        sender.send(
            AnalyticsConstants.config_repeat,
            state.toStateParam()
        )
    }

    fun onSkipClick(state: AnalyticsConfigState) {
        sender.send(
            AnalyticsConstants.config_skip,
            state.toStateParam()
        )
    }

    fun onNextStepClick(state: AnalyticsConfigState) {
        sender.send(
            AnalyticsConstants.config_next,
            state.toStateParam()
        )
    }

}