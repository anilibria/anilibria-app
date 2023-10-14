package ru.radiationx.anilibria.common

import android.content.Context
import android.text.Html
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.schedule.ScheduleDay
import ru.radiationx.shared.ktx.capitalizeDefault
import toothpick.InjectConstructor
import java.text.NumberFormat
import java.util.*

@InjectConstructor
class DetailDataConverter(
    private val context: Context
) {

    @Suppress("DEPRECATION")
    fun toDetail(releaseItem: Release): LibriaDetails = releaseItem.run {
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
            announce = getAnnounce(),
            image = poster.orEmpty(),
            favoriteCount = NumberFormat.getNumberInstance().format(favoriteInfo.rating),
            hasFullHd = episodes.any { it.urlFullHd != null },
            isFavorite = favoriteInfo.isAdded,
            hasEpisodes = episodes.isNotEmpty(),
            hasViewed = episodes.any { it.access.isViewed },
            hasWebPlayer = moonwalkLink != null
        )
    }

    fun Release.getAnnounce(): String {
        if (statusCode == Release.STATUS_CODE_COMPLETE) {
            return "Релиз завершен"
        }

        val originalAnnounce = announce?.trim()?.trim('.')?.capitalizeDefault()
        val scheduleAnnounce = days.firstOrNull()?.toAnnounce2().orEmpty()
        return originalAnnounce ?: scheduleAnnounce
    }

    fun String.toAnnounce(): String {
        val calendarDay = ScheduleDay.toCalendarDay(this)
        val displayDay = Calendar.getInstance().let {
            it.set(Calendar.DAY_OF_WEEK, calendarDay)
            it.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        }.orEmpty()
        val prefix = calendarDay.dayIterationPrefix()
        return "Новая серия $prefix $displayDay"
    }

    fun String.toAnnounce2(): String {
        val calendarDay = ScheduleDay.toCalendarDay(this)
        val prefix = calendarDay.dayIterationPrefix2()
        return "Серии выходят $prefix"
    }

    fun Int.dayIterationPrefix(): String = when (this) {
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.THURSDAY -> "каждый"
        Calendar.WEDNESDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY -> "каждую"
        Calendar.SUNDAY -> "каждое"
        else -> throw Exception("Not found day by $this")
    }

    fun Int.dayIterationPrefix2(): String = when (this) {
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