package ru.radiationx.anilibria.presentation.release.details

import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.entity.app.release.*
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import ru.radiationx.shared.ktx.asTimeSecString
import java.util.*

fun ReleaseFull.toState(): ReleaseDetailState = ReleaseDetailState(
    id = id,
    info = toInfoState(),
    episodesControl = toEpisodeControlState(),
    episodes = mapOf(
        ReleaseFull.Episode.Type.ONLINE to episodes.map { it.toState() },
        ReleaseFull.Episode.Type.SOURCE to episodesSource.map { it.toState() }
    ),
    torrents = torrents.map { it.toState() },
    blockedInfo = blockedInfo.takeIf { it.isBlocked }?.toState()
)

fun FavoriteInfo.toState() = ReleaseFavoriteState(
    rating = rating.toString(),
    isAdded = isAdded
)

fun ReleaseFull.toInfoState(): ReleaseInfoState {
    val seasonsHtml = "<b>Год:</b> " + seasons.joinToString(", ")
    val voicesHtml = "<b>Голоса:</b> " + voices.joinToString(", ")
    val typesHtml = "<b>Тип:</b> " + types.joinToString(", ")
    val releaseStatus = status ?: "Не указано"
    val releaseStatusHtml = "<b>Состояние релиза:</b> $releaseStatus"
    val genresHtml =
        "<b>Жанр:</b> " + genres.joinToString(", ") { "<a href=\"$it\">${it.capitalize()}</a>" }
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
        info = infoStr,
        days = days.map { ScheduleDay.toCalendarDay(it) },
        isOngoing = statusCode == ReleaseItem.STATUS_CODE_PROGRESS,
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

fun ReleaseFull.toEpisodeControlState(): ReleaseEpisodesControlState {
    val hasEpisodes = episodes.isNotEmpty()
    val hasViewed = episodes.any { it.isViewed }
    val hasWeb = !moonwalkLink.isNullOrEmpty()
    val continueTitle = if (hasViewed) {
        val lastViewed = episodes.maxBy { it.lastAccess }
        "Продолжить c ${lastViewed?.id} серии"
    } else {
        "Начать просмотр"
    }

    return ReleaseEpisodesControlState(
        hasWeb = hasWeb,
        hasEpisodes = hasEpisodes,
        hasViewed = hasViewed,
        continueTitle = continueTitle
    )
}

fun TorrentItem.toState(): ReleaseTorrentItemState = ReleaseTorrentItemState(
    id = id,
    title = "Серия $series",
    subtitle = quality.orEmpty(),
    size = Utils.readableFileSize(size),
    seeders = seeders.toString(),
    leechers = leechers.toString(),
    date = null
)

fun ReleaseFull.Episode.toState(): ReleaseEpisodeItemState {
    val subtitle = if (isViewed && seek > 0) {
        "Остановлена на ${Date(seek).asTimeSecString()}"
    } else {
        null
    }
    return ReleaseEpisodeItemState(
        id = id,
        releaseId = releaseId,
        title = title.orEmpty(),
        subtitle = subtitle,
        isViewed = isViewed,
        hasSd = urlSd != null,
        hasHd = urlHd != null,
        hasFullHd = urlFullHd != null
    )
}