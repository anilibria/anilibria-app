package ru.radiationx.data.analytics.features.model

enum class AnalyticsConfigState(val value:String) {
    CHECK_LAST("check_last"),
    LOAD_CONFIG("load_config"),
    CHECK_AVAIL("check_avail"),
    CHECK_PROXIES("check_proxies")
}