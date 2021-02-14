package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class ReleaseAnalytics(
    private val sender: AnalyticsSender
) {

    fun open(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_open,
            "from" to from,
            "id" to releaseId.toString()
        )
    }

    fun copyLink(from: String) {
        sender.send(
            AnalyticsConstants.release_copy,
            "from" to from
        )
    }

    fun share(from: String) {
        sender.send(
            AnalyticsConstants.release_share,
            "from" to from
        )
    }

    fun shortcut(from: String) {
        sender.send(
            AnalyticsConstants.release_shortcut,
            "from" to from
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

    fun episodesTopStart() {
        sender.send(AnalyticsConstants.release_episodes_top_start)
    }

    fun episodesTopContinue() {
        sender.send(AnalyticsConstants.release_episodes_top_continue)
    }

    fun episodesStart() {
        sender.send(AnalyticsConstants.release_episodes_start)
    }

    fun episodesContinue() {
        sender.send(AnalyticsConstants.release_episodes_continue)
    }

    fun episodePlay(quality: Quality?) {
        sender.send(
            AnalyticsConstants.release_episode_play,
            "quality" to quality.toString()
        )
    }

    fun episodeDownload(quality: String?) {
        sender.send(
            AnalyticsConstants.release_episode_download,
            "quality" to quality.toString()
        )
    }

    fun webPlayerClick() {
        sender.send(AnalyticsConstants.release_webplayer)
    }

    fun torrentClick(isHevc: Boolean) {
        sender.send(
            AnalyticsConstants.release_torrent,
            "hevc" to isHevc.toString()
        )
    }

    fun donateClick() {
        sender.send(AnalyticsConstants.release_donate)
    }

    fun descriptionExpand() {
        sender.send(AnalyticsConstants.release_description_expand)
    }

    fun descriptionLinkClick() {
        sender.send(AnalyticsConstants.release_description_link)
    }

    fun scheduleClick() {
        sender.send(AnalyticsConstants.release_schedule_click)
    }

    fun genreClick() {
        sender.send(AnalyticsConstants.release_genre_click)
    }

    fun favoriteAdd() {
        sender.send(AnalyticsConstants.release_favorite_add)
    }

    fun favoriteRemove() {
        sender.send(AnalyticsConstants.release_favorite_remove)
    }

    fun commentsOpen() {
        sender.send(AnalyticsConstants.release_comments_open)
    }

    enum class Quality {
        FULL_HD,
        HD,
        SD
    }
}