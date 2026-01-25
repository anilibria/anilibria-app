package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.extensions.toStateParam
import ru.radiationx.data.analytics.features.extensions.toSuccessParam
import ru.radiationx.data.analytics.features.extensions.toTimeParam
import ru.radiationx.data.analytics.features.model.AnalyticsConfigState
import javax.inject.Inject

class ConfiguringAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    private companion object {
        const val PARAM_ADDRESS = "address"
        const val PARAM_START_ADDRESS = "start_address"
        const val PARAM_END_ADDRESS = "start_address"
    }

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
            startAddressTag.toParam(PARAM_START_ADDRESS),
            endAddressTag.toParam(PARAM_END_ADDRESS),
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam()
        )
    }


    // больше не используется, но на всякий случай пусть будет
    fun checkLast(
        addressTag: String,
        timeInMillis: Long,
        isSuccess: Boolean,
        error: Throwable? = null
    ) {
        sender.send(
            AnalyticsConstants.config_check_last,
            addressTag.toParam(PARAM_ADDRESS),
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
            addressTag.toParam(PARAM_ADDRESS),
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
            addressTag.toParam(PARAM_ADDRESS),
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

    private fun Throwable?.toErrorParam(name: String = "error") =
        Pair(name, this?.javaClass?.simpleName.toString())
}