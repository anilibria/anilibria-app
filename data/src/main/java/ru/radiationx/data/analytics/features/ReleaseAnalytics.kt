package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toIdParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toQualityParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import toothpick.InjectConstructor

@InjectConstructor
class ReleaseAnalytics(
    private val sender: AnalyticsSender
) {

    private companion object {
        const val PARAM_HEVC = "hevc"
    }

    fun open(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_open,
            from.toNavFromParam(),
            releaseId.toIdParam()
        )
    }

    fun copyLink(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_copy,
            from.toNavFromParam(),
            releaseId.toIdParam()
        )
    }

    fun share(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_share,
            from.toNavFromParam(),
            releaseId.toIdParam()
        )
    }

    fun shortcut(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_shortcut,
            from.toNavFromParam(),
            releaseId.toIdParam()
        )
    }

    fun historyReset() {
        sender.send(AnalyticsConstants.release_history_reset)
    }

    fun historyViewAll() {
        sender.send(AnalyticsConstants.release_history_view_all)
    }

    fun historyResetEpisode() {
        sender.send(AnalyticsConstants.release_history_reset_episode)
    }

    fun episodesTopStart(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_top_start,
            releaseId.toIdParam()
        )
    }

    fun episodesTopContinue(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_top_continue,
            releaseId.toIdParam()
        )
    }

    fun episodesStart(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_start,
            releaseId.toIdParam()
        )
    }

    fun episodesContinue(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_continue,
            releaseId.toIdParam()
        )
    }

    fun episodePlay(quality: AnalyticsQuality, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episode_play,
            quality.toQualityParam(),
            releaseId.toIdParam()
        )
    }

    fun episodeDownload(quality: AnalyticsQuality, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episode_download,
            quality.toQualityParam(),
            releaseId.toIdParam()
        )
    }

    fun webPlayerClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_web_player,
            releaseId.toIdParam()
        )
    }

    fun torrentClick(isHevc: Boolean, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_torrent,
            isHevc.toParam(PARAM_HEVC),
            releaseId.toIdParam()
        )
    }

    fun donateClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_donate,
            releaseId.toIdParam()
        )
    }

    fun descriptionExpand() {
        sender.send(AnalyticsConstants.release_description_expand)
    }

    fun descriptionLinkClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_description_link,
            releaseId.toIdParam()
        )
    }

    fun scheduleClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_schedule_click,
            releaseId.toIdParam()
        )
    }

    fun genreClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_genre_click,
            releaseId.toIdParam()
        )
    }

    fun favoriteAdd(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_favorite_add,
            releaseId.toIdParam()
        )
    }

    fun favoriteRemove(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_favorite_remove,
            releaseId.toIdParam()
        )
    }

    fun commentsOpen(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_comments_open,
            releaseId.toIdParam()
        )
    }
}