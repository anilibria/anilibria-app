package ru.radiationx.data.analytics.features.model

enum class AnalyticsSeasonFinishAction(val value: String) {
    RESTART_EPISODE("restart_episode"),
    RESTART_SEASON("restart_seasons"),
    CLOSE_PLAYER("close_player")
}