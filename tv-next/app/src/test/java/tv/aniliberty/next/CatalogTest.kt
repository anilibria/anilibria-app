package tv.aniliberty.next

import java.net.URLDecoder
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class CatalogTest {
    @Test
    fun episodePeriodsOnlyIncludeTheLastMonthWithDisjointGroups() {
        val dates =
            listOf(
                "2026-10-22",
                "2026-10-15",
                "2026-10-02",
                "2026-09-22",
                "2026-09-21",
                "2025-06-01",
            )
        val releases =
            dates.mapIndexed { i, date ->
                parseRelease(
                    org.json.JSONObject().put("id", i + 1).put("updated_at", date + "T12:00:00Z")
                )
            }
        val groups =
            episodePeriods(
                releases,
                java.time.ZoneId.of("Europe/Moscow"),
                java.time.LocalDate.of(2026, 10, 22),
            )
        assertEquals(
            listOf("На этой неделе", "На прошлой неделе", "Ранее за месяц"),
            groups.map { it.title },
        )
        assertEquals(listOf(1, 2, 3, 4), groups.flatMap { it.releases }.map { it.id })
        val rollover =
            episodePeriods(
                listOf(releases.first().copy(updatedAt = "2025-12-31T12:00:00Z")),
                java.time.ZoneId.of("UTC"),
                java.time.LocalDate.of(2026, 1, 2),
            )
        assertEquals("На этой неделе", rollover.single().title)
    }

    @Test
    fun sectionSearchUsesAllWordsAndNormalizesRussianYo() {
        val source =
            SectionData(
                releases =
                    listOf(
                        parseRelease(
                            org.json.JSONObject(
                                """{"id":1,"name":{"main":"Ёж и кот","english":"Hedgehog Cat"}}"""
                            )
                        ),
                        parseRelease(org.json.JSONObject("""{"id":2,"name":{"main":"Другое"}}""")),
                    )
            )
        assertEquals(listOf(1), filterSectionData(source, "еж кот").releases.map { it.id })
        assertEquals(listOf(1), filterSectionData(source, "hedgehog").releases.map { it.id })
        assertEquals(2, filterSectionData(source, "  ").releases.size)
        val merged = mergeSectionData(source, source.copy(more = true))
        assertFalse(merged.more)
        assertEquals(2, merged.releases.size)
    }

    @Test
    fun ongoingUsesAvailableEpisodesInsteadOfPlannedTotal() {
        val release =
            parseRelease(
                org.json.JSONObject(
                    """{"id":1,"is_ongoing":true,"episodes_total":24,"episodes":[{"id":"a","ordinal":1},{"id":"b","ordinal":2},{"id":"special","ordinal":2.5}]}"""
                )
            )
        assertEquals(3, release.catalogueEpisodeCount)
        assertNull(release.copy(releasedEpisodes = null).catalogueEpisodeCount)
        assertEquals(3, release.copy(ongoing = false).catalogueEpisodeCount)
        assertEquals(3, release.copy(ongoing = null).catalogueEpisodeCount)
        assertNull(release.copy(ongoing = false, releasedEpisodes = null).catalogueEpisodeCount)
    }

    @Test
    fun batchCountsUniqueAvailableEpisodesAndDistinguishesMissingFromEmpty() {
        val counts =
            parseReleasedEpisodeCounts(
                """{"data":[{"id":1,"episodes":[{"id":"a"},{"id":"a"},{"id":"b"},{"id":""}]},{"id":2,"episodes":[]},{"id":3}]}"""
            )
        assertEquals(mapOf(1 to 2, 2 to 0), counts)
    }

    @Test
    fun encodesServerFilterValuesAndPreservesGenreDeepLink() {
        val screen =
            Screen.Catalog(
                query = "Мастер & меч",
                genre = 15,
                filters =
                    CatalogFilter(
                        genres = setOf("20", "15"),
                        types = setOf("TV"),
                        seasons = setOf("autumn"),
                        ages = setOf("R18_PLUS"),
                        publish = setOf("IS_ONGOING"),
                        production = setOf("IS_IN_PRODUCTION"),
                        fromYear = "2024",
                        toYear = "2026",
                        sorting = "RATING_DESC",
                    ),
            )
        val path = catalogPath(screen, 3)
        assertTrue(path.contains("%26"))
        val params =
            path.substringAfter("?").split("&").associate { part ->
                val pair = part.split("=", limit = 2)
                URLDecoder.decode(pair[0], "UTF-8") to URLDecoder.decode(pair[1], "UTF-8")
            }
        assertEquals("Мастер & меч", params["f[search]"])
        assertEquals("15,20", params["f[genres]"])
        assertEquals("2024", params["f[years][from_year]"])
        assertEquals("2026", params["f[years][to_year]"])
        assertEquals("TV", params["f[types]"])
        assertEquals("IS_ONGOING", params["f[publish_statuses]"])
        assertEquals("3", params["page"])
    }

    @Test
    fun referenceLabelsComeFromApiAndYearsAreDescending() {
        assertEquals(
            CatalogChoice("TV", "ТВ"),
            parseCatalogChoices("""[{"value":"TV","description":"ТВ"}]""").single(),
        )
        assertEquals(
            CatalogChoice("15", "Фэнтези"),
            parseCatalogChoices("""[{"id":15,"name":"Фэнтези"}]""").single(),
        )
        assertEquals(
            listOf("2026", "2024"),
            parseCatalogChoices("[2024,2026]", true).map { it.value },
        )
    }

    @Test
    fun appendedResultsKeepOldOrderAndDeduplicateIds() {
        val first =
            parseCatalogPage(
                """{"data":[{"id":1},{"id":2}],"meta":{"current_page":1,"last_page":2,"total":3}}""",
                1,
            )
        val next =
            parseCatalogPage(
                """{"data":[{"id":2},{"id":3}],"meta":{"current_page":2,"last_page":2,"total":3}}""",
                2,
            )
        assertTrue(first.more)
        assertFalse(next.more)
        assertEquals(3, first.total)
        assertEquals(
            listOf(1, 2, 3),
            mergeCatalogPages(first.releases, next.releases).map { it.id },
        )
    }

    @Test
    fun readsActualV1NestedPaginationInsteadOfTreatingFirstBatchAsLast() {
        val first =
            parseCatalogPage(
                """{"data":[{"id":1}],"meta":{"pagination":{"total":1704,"count":30,"per_page":30,"current_page":1,"total_pages":57,"links":{"next":"/anime/catalog/releases?page=2"}}}}""",
                1,
            )
        assertTrue(first.more)
        assertEquals(1704, first.total)
        val last =
            parseCatalogPage(
                """{"data":[{"id":1704}],"meta":{"pagination":{"total":1704,"current_page":57,"total_pages":57,"links":{"previous":"?page=56"}}}}""",
                57,
            )
        assertFalse(last.more)
        val empty =
            parseCatalogPage(
                """{"data":[],"meta":{"pagination":{"current_page":1,"total_pages":2}}}""",
                1,
            )
        assertFalse(empty.more)
    }

    @Test
    fun episodeTotalComesFromServerEvenWhenEpisodeListIsNotIncluded() {
        val release = parseRelease(org.json.JSONObject("""{"id":1,"episodes_total":434}"""))
        assertEquals(434, release.totalEpisodes)
        assertTrue(release.episodes.isEmpty())
        assertNull(parseRelease(org.json.JSONObject("""{"id":2}""")).totalEpisodes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsReversedYearRange() {
        catalogPath(Screen.Catalog(filters = CatalogFilter(fromYear = "2026", toYear = "2024")), 1)
    }
}
