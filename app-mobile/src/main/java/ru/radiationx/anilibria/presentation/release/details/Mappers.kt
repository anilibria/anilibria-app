package ru.radiationx.anilibria.presentation.release.details

import ru.radiationx.anilibria.model.asDataColorRes
import ru.radiationx.anilibria.model.asDataIconRes
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.entity.domain.release.*
import ru.radiationx.shared.ktx.asTimeSecString
import ru.radiationx.shared_app.codecs.MediaCodecsFinder
import ru.radiationx.shared_app.codecs.types.CodecProcessingType
import ru.radiationx.shared_app.codecs.types.CodecQuery
import java.util.*

fun Release.toState(): ReleaseDetailState = ReleaseDetailState(
    id = id,
    info = toInfoState(),
    episodesControl = toEpisodeControlState(),
    episodesTabs = toTabsState(),
    torrents = torrents.map { it.toState() },
    blockedInfo = blockedInfo.takeIf { it.isBlocked }?.toState()
)

fun FavoriteInfo.toState() = ReleaseFavoriteState(
    rating = rating.toString(),
    isAdded = isAdded
)

fun Release.toInfoState(): ReleaseInfoState {
    val seasonsHtml = "<b>Год:</b> " + seasons.joinToString(", ")
    val voicesHtml = "<b>Голоса:</b> " + voices.joinToString(", ") {
        val index = voices.indexOf(it)
        "<a href=\"${ReleaseInfoState.TAG_VOICE}_$index\">${it}</a>"
    }
    val typesHtml = "<b>Тип:</b> " + types.joinToString(", ")
    val releaseStatus = status ?: "Не указано"
    val releaseStatusHtml = "<b>Состояние релиза:</b> $releaseStatus"
    val genresHtml = "<b>Жанр:</b> " + genres.joinToString(", ") {
        val index = genres.indexOf(it)
        "<a href=\"${ReleaseInfoState.TAG_GENRE}_$index\">${it.capitalize()}</a>"
    }
    val arrHtml = arrayOf(
        seasonsHtml,
        voicesHtml,
        typesHtml,
        releaseStatusHtml,
        genresHtml
    )
    val infoStr = arrHtml.joinToString("<br>")

    return ReleaseInfoState(
        titleRus = title.orEmpty(),
        titleEng = titleEng.orEmpty(),
        description = description.orEmpty(),
        updatedAt = Date(torrentUpdate * 1000L),
        info = infoStr,
        days = days.map { ru.radiationx.data.entity.domain.schedule.ScheduleDay.toCalendarDay(it) },
        isOngoing = statusCode == Release.STATUS_CODE_PROGRESS,
        announce = announce,
        favorite = favoriteInfo.toState()
    )
}

fun BlockedInfo.toState(): ReleaseBlockedInfoState {
    val defaultReason = """
                    <h4>Контент недоступен на территории Российской Федерации*. Приносим извинения за неудобства.</h4>
                    <br>
                    <span>Подробности смотрите в новостях или социальных сетях</span>""".trimIndent()

    return ReleaseBlockedInfoState(
        title = reason ?: defaultReason
    )
}

fun Release.toEpisodeControlState(): ReleaseEpisodesControlState? {
    val hasEpisodes = episodes.isNotEmpty()
    val hasViewed = episodes.any { it.access.isViewed }
    val hasWeb = !moonwalkLink.isNullOrEmpty()
    val continueTitle = if (hasViewed) {
        val lastViewed = episodes.maxByOrNull { it.access.lastAccess }
        "Продолжить c ${lastViewed?.id?.id} серии"
    } else {
        "Начать просмотр"
    }

    return if (hasWeb || hasEpisodes) {
        ReleaseEpisodesControlState(
            hasWeb = hasWeb,
            hasEpisodes = hasEpisodes,
            hasViewed = hasViewed,
            continueTitle = continueTitle
        )
    } else {
        null
    }
}

fun TorrentItem.toState(): ReleaseTorrentItemState {
    val isTorrentHevc = quality?.contains("hevc", ignoreCase = true) ?: false
    val isSupportHevcHw = MediaCodecsFinder
        .find(CodecQuery("hevc", "hevc"))
        .find { it.processingType == CodecProcessingType.HARDWARE } != null
    val isPrefer = isSupportHevcHw == isTorrentHevc
    return ReleaseTorrentItemState(
        id = id,
        title = "Серия $series",
        subtitle = quality.orEmpty(),
        size = Utils.readableFileSize(size),
        seeders = seeders.toString(),
        leechers = leechers.toString(),
        date = date,
        isPrefer = isPrefer
    )
}

fun Release.toTabsState(): List<EpisodesTabState> {
    val onlineTab = EpisodesTabState(
        tag = "online",
        title = "Онлайн",
        textColor = null,
        episodes = episodes.map { it.toState() }
    )
    val rutubeTab = EpisodesTabState(
        "rutube",
        "RUTUBE",
        textColor = null,
        episodes = rutubePlaylist.map { it.toState() }
    )
    val sourceTab = EpisodesTabState(
        tag = "source",
        title = "Скачать",
        textColor = null,
        episodes = sourceEpisodes.map { it.toState() }
    )
    val externalTabs = externalPlaylists.map { it.toTabState() }

    return listOf(onlineTab, rutubeTab, sourceTab)
        .plus(externalTabs)
        .filter { tab ->
            tab.episodes.isNotEmpty()
                    && tab.episodes.all { it.hasSd || it.hasHd || it.hasFullHd || it.hasActionUrl }
        }
}

fun ExternalPlaylist.toTabState(): EpisodesTabState = EpisodesTabState(
    tag = tag,
    title = title,
    textColor = tag.asDataColorRes(),
    episodes = episodes.map { it.toState(this) }
)

fun ExternalEpisode.toState(
    playlist: ExternalPlaylist
): ReleaseEpisodeItemState = ReleaseEpisodeItemState(
    id = id,
    title = title.orEmpty(),
    subtitle = null,
    updatedAt = null,
    isViewed = false,
    hasUpdate = false,
    hasSd = false,
    hasHd = false,
    hasFullHd = false,
    type = ReleaseEpisodeItemType.EXTERNAL,
    tag = playlist.tag,
    actionTitle = playlist.actionText,
    hasActionUrl = url != null,
    actionIconRes = playlist.tag.asDataIconRes(),
    actionColorRes = playlist.tag.asDataColorRes()
)

fun SourceEpisode.toState(): ReleaseEpisodeItemState = ReleaseEpisodeItemState(
    id = id,
    title = title.orEmpty(),
    subtitle = null,
    updatedAt = updatedAt,
    isViewed = false,
    hasUpdate = false,
    hasSd = urlSd != null,
    hasHd = urlHd != null,
    hasFullHd = urlFullHd != null,
    type = ReleaseEpisodeItemType.SOURCE,
    tag = "source",
    actionTitle = null,
    hasActionUrl = false,
    actionIconRes = null,
    actionColorRes = null
)

fun Episode.toState(): ReleaseEpisodeItemState {
    val subtitle = if (access.isViewed && access.seek > 0) {
        "Остановлена на ${Date(access.seek).asTimeSecString()}"
    } else {
        null
    }
    val hasUpdate = updatedAt?.time?.let { updatedTime ->
        updatedTime > access.lastAccess
    } ?: false
    return ReleaseEpisodeItemState(
        id = id,
        title = title.orEmpty(),
        subtitle = subtitle,
        updatedAt = updatedAt,
        isViewed = access.isViewed,
        hasUpdate = hasUpdate,
        hasSd = urlSd != null,
        hasHd = urlHd != null,
        hasFullHd = urlFullHd != null,
        type = ReleaseEpisodeItemType.ONLINE,
        tag = "online",
        actionTitle = null,
        hasActionUrl = false,
        actionIconRes = null,
        actionColorRes = null
    )
}

fun RutubeEpisode.toState(): ReleaseEpisodeItemState = ReleaseEpisodeItemState(
    id = id,
    title = title.orEmpty(),
    subtitle = null,
    updatedAt = updatedAt,
    isViewed = false,
    hasUpdate = false,
    hasSd = false,
    hasHd = false,
    hasFullHd = false,
    type = ReleaseEpisodeItemType.RUTUBE,
    tag = "rutube",
    actionTitle = "Смотреть",
    actionColorRes = null,
    actionIconRes = null,
    hasActionUrl = true
)