package ru.radiationx.data.analytics.features.model

enum class AnalyticsQuality(val value: String) {
    NO("not_selected"),
    SD("sd"),
    HD("hd"),
    FULL_HD("full_hd"),
    ALWAYS_ASK("always_ask")
}