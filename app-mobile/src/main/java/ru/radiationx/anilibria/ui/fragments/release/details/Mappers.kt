package ru.radiationx.anilibria.ui.fragments.release.details

import androidx.core.text.htmlEncode
import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.model.asDataColorRes
import ru.radiationx.anilibria.model.asDataIconRes
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.apinext.models.ReleaseMember
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.ExternalEpisode
import ru.radiationx.data.entity.domain.release.ExternalPlaylist
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.RutubeEpisode
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
    isInFavorites: Boolean
): ReleaseDetailState = ReleaseDetailState(
    id = id,
    info = toInfoState(isInFavorites),
    episodesControl = toEpisodeControlState(accesses),
    episodesTabs = toTabsState(accesses),
    torrents = torrents.map { it.toState(loadings) },
    blockedInfo = toBlockedInfoState(),
    sponsor = sponsor
)

fun Release.toInfoState(isInFavorites: Boolean): ReleaseInfoState {
    val types = listOfNotNull(
        type,
        averageEpisodeDuration?.let { "~$it мин" },
        ageRating
    )
    val seasonsHtml = "<b>Сезон:</b> $year ${season.orEmpty()}"
    val voicesHtml = members.toInfo().toTypedArray()
    val typesHtml = "<b>Тип:</b> ${types.joinToString()}"
    val releaseStatus = toStatus()
    val releaseStatusHtml = "<b>Состояние релиза:</b> $releaseStatus"
    val genresHtml = "<b>Жанры:</b> " + genres.joinToString {
        val value = it.name.htmlEncode()
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
        titleRus = names.main,
        titleEng = names.english,
        description = description.orEmpty(),
        freshAt = freshAt,
        info = infoStr,
        publishDay = ScheduleDay.toCalendarDay(publishDay),
        needShowDay = isInProduction,
        announce = announce,
        favorite = ReleaseFavoriteState(favoritesCount.toString(), isInFavorites)
    )
}

private fun Release.toStatus(): String {
    return if (isInProduction) "В работе" else "Завершен"
}

private fun List<ReleaseMember>.toInfo(): List<String> {
    return groupBy { it.role.description }
        .mapNotNull { (role, members) ->
            members.map { it.nickname }.asMemberRole(role)
        }
}

private fun List<String>.asMemberRole(title: String): String? {
    if (isEmpty()) return null
    val links = joinToString {
        val value = it.htmlEncode()
        "<a href=\"${ReleaseInfoState.TAG_VOICE}_$value\">${value}</a>"
    }
    return "<b>$title:</b> $links"
}

fun Release.toBlockedInfoState(): ReleaseBlockedInfoState? {
    val reason = blockingReason ?: return null
    return ReleaseBlockedInfoState(title = reason)
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
    val isTorrentHevc = codec?.contains("hevc", ignoreCase = true) ?: false
    val isSupportHevcHw = MediaCodecsFinder
        .find(CodecQuery("hevc", "hevc"))
        .find { it.processingType == CodecProcessingType.HARDWARE } != null
    val isPrefer = isSupportHevcHw == isTorrentHevc
    return ReleaseTorrentItemState(
        id = id,
        title = "Серии $series",
        subtitle = listOfNotNull(type, quality, codec).joinToString(" "),
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
    val externalTabs = externalPlaylists.map { it.toTabState() }

    return listOf(onlineTab, rutubeTab)
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