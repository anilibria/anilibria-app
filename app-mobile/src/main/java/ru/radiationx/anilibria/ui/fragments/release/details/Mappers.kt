package ru.radiationx.anilibria.ui.fragments.release.details

import androidx.core.text.htmlEncode
import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.model.asDataColorRes
import ru.radiationx.anilibria.model.asDataIconRes
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.BlockedInfo
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.ExternalEpisode
import ru.radiationx.data.entity.domain.release.ExternalPlaylist
import ru.radiationx.data.entity.domain.release.FavoriteInfo
import ru.radiationx.data.entity.domain.release.Members
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.RutubeEpisode
import ru.radiationx.data.entity.domain.release.SourceEpisode
import ru.radiationx.data.entity.domain.release.TorrentItem
import ru.radiationx.data.entity.domain.schedule.ScheduleDay
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.TorrentId
import ru.radiationx.shared.ktx.asTimeSecString
import ru.radiationx.shared.ktx.capitalizeDefault
import ru.radiationx.shared_app.codecs.MediaCodecsFinder
import ru.radiationx.shared_app.codecs.types.CodecProcessingType
import ru.radiationx.shared_app.codecs.types.CodecQuery
import java.util.Date

fun Release.toState(
    loadings: Map<TorrentId, MutableStateFlow<Int>>,
    accesses: Map<EpisodeId, EpisodeAccess>,
): ReleaseDetailState = ReleaseDetailState(
    id = id,
    info = toInfoState(),
    episodesControl = toEpisodeControlState(accesses),
    episodesTabs = toTabsState(accesses),
    torrents = torrents.map { it.toState(loadings) },
    blockedInfo = blockedInfo.takeIf { it.isBlocked }?.toState()
)

fun FavoriteInfo.toState() = ReleaseFavoriteState(
    rating = rating.toString(),
    isAdded = isAdded
)

fun Release.toInfoState(): ReleaseInfoState {
    val seasonsHtml = "<b>Сезон:</b> ${year.orEmpty()} ${season.orEmpty()}"
    val voicesHtml = members?.toInfo().orEmpty().toTypedArray()
    val typesHtml = "<b>Тип:</b> " + types.joinToString(", ")
    val releaseStatus = status?.takeIf { it.isNotEmpty() } ?: "Не указано"
    val releaseStatusHtml = "<b>Состояние релиза:</b> $releaseStatus"
    val genresHtml = "<b>Жанры:</b> " + genres.joinToString(", ") {
        val value = it.htmlEncode()
        "<a href=\"${ReleaseInfoState.TAG_GENRE}_$value\">${value.capitalizeDefault()}</a>"
    }
    val arrHtml = listOfNotNull(
        seasonsHtml,
        typesHtml,
        genresHtml,
        *voicesHtml,
        releaseStatusHtml
    )
    val infoStr = arrHtml.joinToString("<br>")

    return ReleaseInfoState(
        titleRus = title.orEmpty(),
        titleEng = titleEng.orEmpty(),
        description = description.orEmpty(),
        updatedAt = updatedAt.takeIf { it != 0 }?.let { Date(it * 1000L) },
        info = infoStr,
        days = publishDay.map { ScheduleDay.toCalendarDay(it) },
        isOngoing = statusCode == Release.STATUS_CODE_PROGRESS,
        announce = announce,
        favorite = favoriteInfo.toState()
    )
}

private fun Members.toInfo(): List<String> {
    return listOfNotNull(
        voicing.asMemberRole("Озвучка"),
        timing.asMemberRole("Тайминг"),
        (translating + editing + decorating).asMemberRole("Работа над субтитрами"),
    )
}

private fun List<String>.asMemberRole(title: String): String? {
    if (isEmpty()) return null
    val links = joinToString(", ") {
        val value = it.htmlEncode()
        "<a href=\"${ReleaseInfoState.TAG_VOICE}_$value\">${value}</a>"
    }
    return "<b>$title:</b> $links"
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

fun Release.toEpisodeControlState(
    accesses: Map<EpisodeId, EpisodeAccess>,
): ReleaseEpisodesControlState? {
    val hasEpisodes = episodes.isNotEmpty()
    val hasViewed = episodes.any {
        accesses[it.id]?.isViewed == true
    }
    val hasWeb = !webPlayer.isNullOrEmpty()
    val continueTitle = if (hasViewed) {
        val lastViewed = episodes.maxByOrNull {
            accesses[it.id]?.lastAccess ?: 0
        }
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

fun TorrentItem.toState(
    loadings: Map<TorrentId, MutableStateFlow<Int>>,
): ReleaseTorrentItemState {
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
        isPrefer = isPrefer,
        progress = loadings[id]
    )
}

fun Release.toTabsState(
    accesses: Map<EpisodeId, EpisodeAccess>,
): List<EpisodesTabState> {
    val onlineTab = EpisodesTabState(
        tag = "online",
        title = "Онлайн",
        textColor = null,
        episodes = episodes.map { it.toState(accesses) }
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
    playlist: ExternalPlaylist,
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
    hasSd = PlayerQuality.SD in qualityInfo,
    hasHd = PlayerQuality.HD in qualityInfo,
    hasFullHd = PlayerQuality.FULLHD in qualityInfo,
    type = ReleaseEpisodeItemType.SOURCE,
    tag = "source",
    actionTitle = null,
    hasActionUrl = false,
    actionIconRes = null,
    actionColorRes = null
)

fun Episode.toState(
    accesses: Map<EpisodeId, EpisodeAccess>,
): ReleaseEpisodeItemState {
    val access = accesses[id]
    val subtitle = if (access != null && access.isViewed && access.seek > 0) {
        "Остановлена на ${Date(access.seek).asTimeSecString()}"
    } else {
        null
    }
    val hasUpdate = updatedAt?.time?.let { updatedTime ->
        access != null && updatedTime > access.lastAccess
    } ?: false
    return ReleaseEpisodeItemState(
        id = id,
        title = title.orEmpty(),
        subtitle = subtitle,
        updatedAt = updatedAt,
        isViewed = access?.isViewed == true,
        hasUpdate = hasUpdate,
        hasSd = false,
        hasHd = false,
        hasFullHd = false,
        type = ReleaseEpisodeItemType.ONLINE,
        tag = "online",
        actionTitle = "Смотреть",
        hasActionUrl = true,
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