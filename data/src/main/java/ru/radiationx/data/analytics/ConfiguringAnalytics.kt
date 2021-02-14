package ru.radiationx.data.analytics

import toothpick.InjectConstructor
import java.lang.Exception

@InjectConstructor
class ConfiguringAnalytics(
    private val analyticsSender: AnalyticsSender
) {

    private fun Long.toTimeParam() = Pair("time", this.toString())
    private fun Boolean.toSuccessParam() = Pair("success", this.toString())
    private fun String?.toAddressParam() = Pair("address", this.toString())
    private fun Throwable?.toErrorParam() = Pair("error", this.toString())

    private fun <T> T?.asParam(name: String) = Pair<String, String>(name, this.toString())

    fun checkFull(
        startAddressTag: String,
        endAddressTag: String,
        timeInMillis: Long,
        isSuccess: Boolean
    ) {
        analyticsSender.send(
            "check_full",
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
        exception: Throwable? = null
    ) {
        analyticsSender.send(
            "check_last",
            addressTag.toAddressParam(),
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam(),
            exception.toErrorParam()
        )
    }

    fun loadConfig(
        timeInMillis: Long,
        isSuccess: Boolean,
        exception: Throwable? = null
    ) {
        analyticsSender.send(
            "load_config",
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam(),
            exception.toErrorParam()
        )
    }

    fun checkAvail(
        addressTag: String?,
        timeInMillis: Long,
        isSuccess: Boolean,
        exception: Throwable? = null
    ) {
        analyticsSender.send(
            "check_avail",
            addressTag.toAddressParam(),
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam(),
            exception.toErrorParam()
        )
    }

    fun checkProxies(
        addressTag: String?,
        timeInMillis: Long,
        isSuccess: Boolean,
        exception: Throwable? = null
    ) {
        analyticsSender.send(
            "check_proxies",
            addressTag.toAddressParam(),
            timeInMillis.toTimeParam(),
            isSuccess.toSuccessParam(),
            exception.toErrorParam()
        )
    }


    fun onRepeatClick(state: String) {
        analyticsSender.send("repeat_click", "state" to state)
    }

    fun onSkipClick(state: String) {
        analyticsSender.send("skip_click", "state" to state)
    }

    fun onNextStepClick(state: String) {
        analyticsSender.send("next_click", "state" to state)
    }
}