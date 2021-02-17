package ru.radiationx.data.analytics.features.model

enum class AnalyticsPlayer(val value: String) {
    NO("not_selected"),
    EXTERNAL("external"),
    INTERNAL("internal"),
    ALWAYS_ASK("always_ask")
}