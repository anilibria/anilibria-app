package ru.radiationx.data.analytics.features.model

enum class AnalyticsAuthState(val value:String) {
    NO_AUTH("no"),
    AUTH_SKIPPED("skip"),
    AUTH("auth")
}