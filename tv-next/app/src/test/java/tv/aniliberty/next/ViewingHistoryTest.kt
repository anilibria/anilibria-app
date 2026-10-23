package tv.aniliberty.next

import android.app.Application
import android.content.Context
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], application = Application::class)
class ViewingHistoryTest {
    private val prefs =
        RuntimeEnvironment.getApplication()
            .getSharedPreferences("continue-test", Context.MODE_PRIVATE)
    private val history = ViewingHistory(prefs)
    private val release =
        parseRelease(
            JSONObject(
                """{"id":42,"name":{"main":"Тайтл"},"episodes":[
        {"id":"first","ordinal":1,"duration":1200,"preview":{"preview":"/first.webp"},"hls_720":"https://example.org/private-stream.m3u8"},
        {"id":"special","ordinal":12.5,"name":"Особая серия","duration":1200,"preview":{"preview":"/special.webp"},"hls_720":"https://example.org/next.m3u8"}
    ]}"""
            )
        )

    @Before
    fun clear() {
        prefs.edit().clear().commit()
    }

    @Test
    fun configuredThresholdOffersNextEpisodeWithoutLosingActualPosition() {
        prefs.edit().putInt(WATCHED_PERCENT_KEY, 85).commit()
        history.save(release, release.episodes[0], 1019999, 1200000, 10)
        assertEquals("first", history.items().single().episodeId)
        history.save(release, release.episodes[0], 1020000, 1200000, 11)
        assertEquals("special", history.items().single().episodeId)
        assertEquals(1020000L, history.latestWatched().single().position)
        assertTrue(history.latestWatched().single().completed)
        prefs.edit().putInt(WATCHED_PERCENT_KEY, 95).commit()
        assertEquals("first", history.items().single().episodeId)
        assertFalse(history.latestWatched().single().completed)
    }

    @Test
    fun dismissedReleaseStaysHiddenAfterMetadataRefreshUntilNewPlayback() {
        prefs.edit().putLong("position:first", 300000).apply()
        history.save(release, release.episodes[0], 300000, 1200000, 10)
        history.dismiss(42)
        assertTrue(history.items().isEmpty())
        history.save(release, release.episodes[0], 300000, 1200000, 10)
        assertTrue(history.items().isEmpty())
        assertEquals(300000L, prefs.getLong("position:first", 0))
        history.save(
            release,
            release.episodes[0],
            310000,
            1200000,
            prefs.getLong("continue-hidden:42", 0) + 1,
        )
        assertEquals("first", history.items().single().episodeId)
    }

    @Test
    fun openingReleaseOrPublishingAnEpisodeRestoresDismissedCard() {
        history.save(release, release.episodes[0], 300000, 1200000, 10)
        history.dismiss(42)
        history.restore(42)
        assertEquals("first", history.items().single().episodeId)
        history.dismiss(42)
        val expanded =
            release.copy(
                episodes =
                    release.episodes +
                        release.episodes.last().copy(id = "new-episode", ordinal = 13.0)
            )
        history.save(expanded, expanded.episodes[0], 300000, 1200000, 10)
        assertEquals("first", history.items().single().episodeId)
    }

    @Test
    fun unfinishedEpisodeKeepsExactUuidAndActualPreview() {
        history.save(release, release.episodes[0], 300000, 1200000, 10)
        val item = history.items().single()
        assertEquals("first", item.episodeId)
        assertFalse(item.nextUp)
        assertEquals("https://cdn.anilibria.top/first.webp", item.preview)
        assertEquals(300000L, item.position)
        assertFalse(prefs.getString("continue-episodes", "")!!.contains("private-stream"))
    }

    @Test
    fun completedEpisodeOffersNextInServerOrderIncludingFractionalOrdinal() {
        history.save(release, release.episodes[0], 1200000, 1200000, 10)
        val item = history.items().single()
        assertEquals("special", item.episodeId)
        assertTrue(item.nextUp)
        assertEquals(0L, item.position)
        assertTrue(item.episodeTitle.contains("12.5"))
        assertEquals("https://cdn.anilibria.top/special.webp", item.preview)
    }

    @Test
    fun allFinishedHidesReleaseAndNewSessionReplacesNextUp() {
        history.save(release, release.episodes[0], 1200000, 1200000, 10)
        history.save(release, release.episodes[1], 60000, 1200000, 11)
        assertEquals(1, history.items().size)
        assertFalse(history.items().single().nextUp)
        history.save(release, release.episodes[1], 1200000, 1200000, 12)
        assertTrue(history.items().isEmpty())
    }

    @Test
    fun openingNextThenLeavingAtZeroKeepsItAsNextUp() {
        history.save(release, release.episodes[0], 1200000, 1200000, 10)
        history.save(release, release.episodes[1], 0, 1200000, 11)
        val item = history.items().single()
        assertEquals("special", item.episodeId)
        assertTrue(item.nextUp)
    }

    @Test
    fun unavailableNextEpisodeDoesNotProduceWatchTarget() {
        val unavailable =
            release.copy(
                episodes =
                    listOf(release.episodes[0], release.episodes[1].copy(streams = emptyList()))
            )
        history.save(unavailable, unavailable.episodes[0], 1200000, 1200000, 10)
        assertTrue(history.items().isEmpty())
    }

    @Test
    fun scheduleHeaderBelongsToItsOwnShelf() {
        val rows =
            listOf(
                Row("latest", Kind.HEADING),
                Row("latest-shelf", Kind.SHELF),
                Row("continue", Kind.HEADING),
                Row("continue-shelf", Kind.SHELF),
                Row("schedule", Kind.DAYS),
                Row("schedule-shelf", Kind.SHELF),
            )
        assertEquals(0, homeSectionHeader(rows, 1))
        assertEquals(2, homeSectionHeader(rows, 3))
        assertEquals(4, homeSectionHeader(rows, 5))
    }
}
