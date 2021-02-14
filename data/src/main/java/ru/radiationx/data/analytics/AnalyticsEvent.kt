package ru.radiationx.data.analytics

sealed class AnalyticsEvent(
    val key: String
) {
    private val params = mutableMapOf<String, String>()

    fun putParam(key: String, value: String): AnalyticsEvent {
        params[key] = value
        return this
    }
}
