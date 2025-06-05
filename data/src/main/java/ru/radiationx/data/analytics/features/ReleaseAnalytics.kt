package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toIdParam
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.extensions.toQualityParam
import ru.radiationx.data.analytics.features.model.AnalyticsQuality
import javax.inject.Inject

class ReleaseAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    private companion object {
        const val PARAM_HEVC = "hevc"
        const val PARAM_RELEASE_CODE = "code"
        const val PARAM_EXTERNAL_TAG = "tag"
    }

    fun open(from: String, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_open,
            from.toNavFromParam(),
            releaseId.toIdParam(),
        )
    }

    fun open(from: String, releaseCode: String) {
        sender.send(
            AnalyticsConstants.release_open,
            from.toNavFromParam(),
            releaseCode.toParam(PARAM_RELEASE_CODE)
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

    fun episodesTopStartClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_top_start,
            releaseId.toIdParam()
        )
    }

    fun episodesTopContinueClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_top_continue,
            releaseId.toIdParam()
        )
    }

    fun episodesStartClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_start,
            releaseId.toIdParam()
        )
    }

    fun episodesContinueClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episodes_continue,
            releaseId.toIdParam()
        )
    }

    fun episodePlayClick(quality: AnalyticsQuality, releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episode_play,
            quality.toQualityParam(),
            releaseId.toIdParam()
        )
    }

    fun episodeExternalClick(releaseId: Int, externalTag: String) {
        sender.send(
            AnalyticsConstants.release_episode_external,
            releaseId.toIdParam(),
            externalTag.toParam(PARAM_EXTERNAL_TAG)
        )
    }

    fun episodeRutubeClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_episode_rutube,
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

    fun sponsorClick(releaseId: Int, sponsorTitle: String) {
        sender.send(
            AnalyticsConstants.release_sponsor,
            releaseId.toIdParam(),
            sponsorTitle.toParam("sponsor")
        )
    }

    fun donateClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_donate,
            releaseId.toIdParam()
        )
    }

    fun descriptionExpand(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_description_expand,
            releaseId.toIdParam()
        )
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

    fun voiceClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_voice_click,
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

    fun commentsClick(releaseId: Int) {
        sender.send(
            AnalyticsConstants.release_comments_click,
            releaseId.toIdParam()
        )
    }

    fun episodesTabClick(releaseId: Int, tabTag: String) {
        sender.send(
            AnalyticsConstants.release_episodes_tab_click,
            releaseId.toIdParam(),
            tabTag.toParam(PARAM_EXTERNAL_TAG)
        )
    }
}