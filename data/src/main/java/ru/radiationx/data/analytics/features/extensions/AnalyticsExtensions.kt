package ru.radiationx.data.analytics.features.extensions

import ru.radiationx.data.analytics.features.model.AnalyticsAppTheme
import ru.radiationx.data.analytics.features.model.AnalyticsConfigState
import ru.radiationx.data.analytics.features.model.AnalyticsPlayer
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import java.util.Locale


fun String?.toNavFromParam() = Pair("from", this.toString())

fun String?.toLinkParam() = Pair("link", this.toString())

fun Boolean?.toSuccessParam(name: String = "success") = Pair(name, this.toString())

fun Any?.toParam(name: String) = Pair(name, this.toString())

fun Int?.toPageParam(name: String = "page") = Pair(name, this.toString())

fun Int?.toIdParam(name: String = "id") = Pair(name, this.toString())

fun String?.toVidParam(name: String = "vid") = Pair(name, this.toString())

fun Int?.toPositionParam(name: String = "position") = Pair(name, this.toString())

fun Long?.toTimeParam(name: String = "time") = Pair(name, this?.toSecondsString().toString())

fun Long?.toPreciseTimeParam(name: String = "time") = Pair(name, this?.toPreciseSecondsString().toString())

fun Throwable?.toErrorParam(name: String = "error") = Pair(name, this.toString())

fun AnalyticsConfigState?.toStateParam(name: String = "state") = Pair(name, this.toString())

fun AnalyticsQuality?.toQualityParam(name: String = "quality") = Pair(name, this?.value.toString())

fun AnalyticsPlayer?.toPlayerParam(name: String = "player") = Pair(name, this?.value.toString())

fun AnalyticsAppTheme?.toThemeParam(name: String = "theme") = Pair(name, this?.value.toString())

fun Long.toPreciseSecondsString(): String {
    val timeInSeconds = this / 1000f
    return "%.2f".format(Locale.US, timeInSeconds)
}

fun Long.toSecondsString(): String {
    val timeInSeconds = this / 1000f
    return "%.1f".format(Locale.US, timeInSeconds)
}