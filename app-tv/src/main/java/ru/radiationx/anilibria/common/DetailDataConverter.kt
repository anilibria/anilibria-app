package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.schedule.ScheduleDay
import ru.radiationx.shared.ktx.capitalizeDefault
import toothpick.InjectConstructor
import java.text.NumberFormat
import java.util.Calendar

@InjectConstructor
class DetailDataConverter {

    fun toDetail(
        releaseItem: Release,
        isFull: Boolean,
        accesses: List<EpisodeAccess>,
        isInFavorites: Boolean
    ): LibriaDetails = releaseItem.run {
        val types = listOfNotNull(
            type,
            averageEpisodeDuration?.let { "~$it мин" },
            ageRating
        )
        LibriaDetails(
            id = id,
            titleRu = names.main,
            titleEn = names.english,
            extra = listOf(
                genres.firstOrNull()?.capitalizeDefault()?.trim(),
                "$year ${season.orEmpty()}",
                types.joinToString(),
                "Серий: ${episodes.ifEmpty { null }?.size ?: "Не доступно"}"
            ).joinToString(" • "),
            description = description.orEmpty(),
            announce = getAnnounce(isFull),
            image = poster.orEmpty(),
            favoriteCount = NumberFormat.getNumberInstance().format(favoritesCount),
            hasFullHd = episodes.any { PlayerQuality.FULLHD in it.qualityInfo },
            isFavorite = isInFavorites,
            hasEpisodes = episodes.isNotEmpty(),
            hasViewed = accesses.any { it.isViewed },
            hasWebPlayer = webPlayer != null
        )
    }

    private fun Release.getAnnounce(isFull: Boolean): String {
        if (!isFull) return ""
        val announceText = if (!isInProduction) {
            val originalAnnounce = announce?.trim()?.trim('.')?.capitalizeDefault()
            val scheduleAnnounce = publishDay.toAnnounce2()
            originalAnnounce ?: scheduleAnnounce
        } else {
            "Релиз завершен"
        }
        val episodesWarning = if (episodes.isEmpty()) {
            "Нет доступных для просмотра серий"
        } else {
            null
        }
        return listOfNotNull(announceText, episodesWarning).joinToString(" • ")
    }

    private fun String.toAnnounce2(): String {
        val calendarDay = ScheduleDay.toCalendarDay(this)
        val prefix = calendarDay.dayIterationPrefix2()
        return "Серии выходят $prefix"
    }

    private fun Int.dayIterationPrefix(): String = when (this) {
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.THURSDAY,
        -> "каждый"

        Calendar.WEDNESDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        -> "каждую"

        Calendar.SUNDAY -> "каждое"
        else -> throw Exception("Not found day by $this")
    }

    private fun Int.dayIterationPrefix2(): String = when (this) {
        Calendar.MONDAY -> "в понедельник"
        Calendar.TUESDAY -> "во вторник"
        Calendar.WEDNESDAY -> "в среду"
        Calendar.THURSDAY -> "в четверг"
        Calendar.FRIDAY -> "в пятницу"
        Calendar.SATURDAY -> "в субботу"
        Calendar.SUNDAY -> "в воскресенье"
        else -> throw Exception("Not found day by $this")
    }
}