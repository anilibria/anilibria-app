package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class ReleaseAnalytics(
    private val sender: AnalyticsSender
) {

    private fun Int.toReleaseIdParam() = Pair("id", this.toString())
    private fun Quality?.toQualityParam() = Pair("quality", this.toString())

    fun open(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_open,
            "from" to from,
            releaseId.toReleaseIdParam()
        )
    }

    fun copyLink(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_copy,
            "from" to from,
            releaseId.toReleaseIdParam()
        )
    }

    fun share(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_share,
            "from" to from,
            releaseId.toReleaseIdParam()
        )
    }

    fun shortcut(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_shortcut,
            "from" to from,
            releaseId.toReleaseIdParam()
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
            releaseId.toReleaseIdParam()
        )
    }

    fun episodesTopContinue(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_top_continue,
            releaseId.toReleaseIdParam()
        )
    }

    fun episodesStart(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_start,
            releaseId.toReleaseIdParam()
        )
    }

    fun episodesContinue(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_continue,
            releaseId.toReleaseIdParam()
        )
    }

    fun episodePlay(quality: Quality?, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episode_play,
            quality.toQualityParam(),
            releaseId.toReleaseIdParam()
        )
    }

    fun episodeDownload(quality: Quality?, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episode_download,
            quality.toQualityParam(),
            releaseId.toReleaseIdParam()
        )
    }

    fun webPlayerClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_web_player,
            releaseId.toReleaseIdParam()
        )
    }

    fun torrentClick(isHevc: Boolean, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_torrent,
            "hevc" to isHevc.toString(),
            releaseId.toReleaseIdParam()
        )
    }

    fun donateClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_donate,
            releaseId.toReleaseIdParam()
        )
    }

    fun descriptionExpand() {
        sender.send(AnalyticsConstants.release_description_expand)
    }

    fun descriptionLinkClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_description_link,
            releaseId.toReleaseIdParam()
        )
    }

    fun scheduleClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_schedule_click,
            releaseId.toReleaseIdParam()
        )
    }

    fun genreClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_genre_click,
            releaseId.toReleaseIdParam()
        )
    }

    fun favoriteAdd(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_favorite_add,
            releaseId.toReleaseIdParam()
        )
    }

    fun favoriteRemove(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_favorite_remove,
            releaseId.toReleaseIdParam()
        )
    }

    fun commentsOpen(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_comments_open,
            releaseId.toReleaseIdParam()
        )
    }

    enum class Quality {
        FULL_HD,
        HD,
        SD
    }
}