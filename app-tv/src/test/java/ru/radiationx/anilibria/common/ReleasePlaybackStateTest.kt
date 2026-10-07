package ru.radiationx.anilibria.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId

class ReleasePlaybackStateTest {
    private val episodes = (1..3).map { EpisodeId(it.toString(), ReleaseId(1)) }
    private fun access(index: Int, viewed: Boolean = true, seek: Long = 0L, date: Long = 1L) =
        EpisodeAccess(episodes[index], seek, viewed, date)

    @Test fun `all viewed episodes restart from first without requiring a saved position`() {
        val state = ReleasePlaybackState.resolve(episodes, episodes.indices.map { access(it) })
        assertTrue(state.allEpisodesViewed)
        assertFalse(state.hasProgress)
        assertEquals(episodes.first(), state.continueEpisodeId)
    }

    @Test fun `all viewed episodes restart instead of resuming most recent episode`() {
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0), access(1), access(2, seek = 100L, date = 5L)))
        assertTrue(state.allEpisodesViewed)
        assertEquals(episodes.first(), state.continueEpisodeId)
    }

    @Test fun `one unfinished episode restores continue and uses most recent progress`() {
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0), access(1), access(2, false, 100L, 5L)))
        assertFalse(state.allEpisodesViewed)
        assertTrue(state.hasProgress)
        assertEquals(episodes.last(), state.continueEpisodeId)
    }

    @Test fun `newly available episode removes all viewed state`() {
        assertFalse(ReleasePlaybackState.resolve(episodes, listOf(access(0), access(1))).allEpisodesViewed)
    }

    @Test fun `stale or foreign marks cannot satisfy missing available episodes`() {
        val foreign = EpisodeAccess(EpisodeId("3", ReleaseId(2)), 100L, true, 10L)
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0), access(1), foreign))
        assertFalse(state.allEpisodesViewed)
        assertFalse(state.hasProgress)
        assertEquals(episodes.last(), state.continueEpisodeId)
        assertEquals("Продолжить (3 серия)", state.continueText)
    }

    @Test fun `empty playlist never offers restart`() {
        val state = ReleasePlaybackState.resolve(emptyList(), listOf(access(0)))
        assertFalse(state.allEpisodesViewed)
        assertNull(state.continueEpisodeId)
        assertFalse(state.showPlayAction)
    }

    @Test fun `single unstarted episode keeps watch action`() {
        val state = ReleasePlaybackState.resolve(episodes.take(1), emptyList())
        assertTrue(state.showPlayAction)
        assertNull(state.continueText)
    }

    @Test fun `single unfinished episode shows time and hides watch action`() {
        val state = ReleasePlaybackState.resolve(episodes.take(1), listOf(access(0, false, 754_000L)))
        assertEquals("Продолжить (12:34)", state.continueText)
        assertEquals(episodes.first(), state.continueEpisodeId)
        assertFalse(state.showPlayAction)
    }

    @Test fun `single viewed episode offers restart and hides watch action`() {
        val state = ReleasePlaybackState.resolve(episodes.take(1), listOf(access(0)))
        assertEquals("Заново", state.continueText)
        assertEquals(episodes.first(), state.continueEpisodeId)
        assertFalse(state.showPlayAction)
    }

    @Test fun `multiple episodes show number and unfinished position`() {
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0, false, 754_000L)))
        assertEquals("Продолжить (1 серия - 12:34)", state.continueText)
        assertEquals(episodes.first(), state.continueEpisodeId)
        assertTrue(state.showPlayAction)
    }

    @Test fun `viewed episode advances to unstarted next episode`() {
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0, seek = 1_400_000L)))
        assertEquals("Продолжить (2 серия)", state.continueText)
        assertEquals(episodes[1], state.continueEpisodeId)
    }

    @Test fun `next unfinished episode retains its time even if previous was accessed later`() {
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0, seek = 1_400_000L, date = 5L), access(1, false, 754_000L)))
        assertEquals("Продолжить (2 серия - 12:34)", state.continueText)
        assertEquals(episodes[1], state.continueEpisodeId)
    }

    @Test fun `already viewed next episodes are skipped`() {
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0, seek = 1_400_000L, date = 5L), access(1)))
        assertEquals("Продолжить (3 серия)", state.continueText)
        assertEquals(episodes.last(), state.continueEpisodeId)
    }

    @Test fun `unfinished earlier gap is offered when latest episode is viewed`() {
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0), access(2, seek = 1_400_000L, date = 5L)))
        assertEquals("Продолжить (2 серия)", state.continueText)
        assertEquals(episodes[1], state.continueEpisodeId)
    }

    @Test fun `manually viewed episode can advance without a timecode`() {
        val state = ReleasePlaybackState.resolve(episodes, listOf(access(0)))
        assertEquals("Продолжить (2 серия)", state.continueText)
        assertEquals(episodes[1], state.continueEpisodeId)
    }
}
