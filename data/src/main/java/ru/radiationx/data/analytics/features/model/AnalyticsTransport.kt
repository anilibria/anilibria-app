package ru.radiationx.data.analytics.features.model

enum class AnalyticsTransport(val value: String) {
    SYSTEM("system"),
    OKHTTP("okhttp"),
    CRONET("cronet")
}