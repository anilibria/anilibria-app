package ru.radiationx.data.analytics.features.model

enum class AnalyticsQuality(val value: String) {
    NONE("none"),
    SD("sd"),
    HD("hd"),
    FULL_HD("full_hd"),
    ALWAYS_ASK("always_ask"),
    UNKNOWN("unknown")
}