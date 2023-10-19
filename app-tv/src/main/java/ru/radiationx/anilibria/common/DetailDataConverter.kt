package ru.radiationx.anilibria.common

import android.text.Html
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.schedule.ScheduleDay
import ru.radiationx.shared.ktx.capitalizeDefault
import toothpick.InjectConstructor
import java.text.NumberFormat
import java.util.*

@InjectConstructor
class DetailDataConverter {

    @Suppress("DEPRECATION")
    fun toDetail(releaseItem: Release, isFull: Boolean): LibriaDetails = releaseItem.run {
        LibriaDetails(
            id = id,
            titleRu = title.orEmpty(),
            titleEn = titleEng.orEmpty(),
            extra = listOf(
                genres.firstOrNull()?.capitalizeDefault()?.trim(),
                "${seasons.firstOrNull()} год",
                types.firstOrNull()?.trim(),
                "Серии: ${series?.trim() ?: "Не доступно"}"
            ).joinToString(" • "),
            description = Html.fromHtml(description.orEmpty()).toString().trim()
                .trim('"')/*.replace('\n', ' ')*/,
            announce = getAnnounce(isFull),
            image = poster.orEmpty(),
            favoriteCount = NumberFormat.getNumberInstance().format(favoriteInfo.rating),
            hasFullHd = episodes.any { it.urlFullHd != null },
            isFavorite = favoriteInfo.isAdded,
            hasEpisodes = episodes.isNotEmpty(),
            hasViewed = episodes.any { it.access.isViewed },
            hasWebPlayer = moonwalkLink != null
        )
    }

    private fun Release.getAnnounce(isFull: Boolean): String {
        if (!isFull) return ""
        val announceText = if (statusCode == Release.STATUS_CODE_COMPLETE) {
            "Релиз завершен"
        } else {
            val originalAnnounce = announce?.trim()?.trim('.')?.capitalizeDefault()
            val scheduleAnnounce = days.firstOrNull()?.toAnnounce2().orEmpty()
            originalAnnounce ?: scheduleAnnounce
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