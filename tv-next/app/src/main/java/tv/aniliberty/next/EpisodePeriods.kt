package tv.aniliberty.next

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class EpisodePeriod(val key: String, val title: String, val releases: List<Release>)

internal fun episodeUpdateDate(release: Release, zone: ZoneId): LocalDate? {
    val timestamp =
        release.latestEpisode?.updatedAt?.takeIf { it.isNotBlank() } ?: release.updatedAt
    return runCatching { OffsetDateTime.parse(timestamp).atZoneSameInstant(zone).toLocalDate() }
        .getOrNull()
}

internal fun recentEpisodeReleases(
    releases: List<Release>,
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone),
) =
    releases.filter {
        episodeUpdateDate(it, zone)?.let { date ->
            date >= today.minusMonths(1) && date <= today
        } == true
    }

/** Disjoint groups within the most recent month, using the TV's calendar timezone. */
fun episodePeriods(
    releases: List<Release>,
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone),
): List<EpisodePeriod> {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val groups =
        recentEpisodeReleases(releases, zone, today).groupBy { release ->
            val date = episodeUpdateDate(release, zone)!!
            when {
                date >= monday -> 0
                date >= monday.minusWeeks(1) -> 1
                else -> 2
            }
        }
    val titles = listOf("На этой неделе", "На прошлой неделе", "Ранее за месяц")
    return groups.entries
        .sortedBy { it.key }
        .map { (period, items) ->
            EpisodePeriod(
                "recent:$period",
                titles[period],
                items.sortedByDescending {
                    it.latestEpisode?.updatedAt?.ifBlank { it.updatedAt } ?: it.updatedAt
                },
            )
        }
}
