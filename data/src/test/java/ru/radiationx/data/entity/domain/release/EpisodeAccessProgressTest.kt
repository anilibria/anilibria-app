package ru.radiationx.data.entity.domain.release

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId

class EpisodeAccessProgressTest {
    private val id = EpisodeId("1", ReleaseId(1))

    @Test fun `saved progress is independent of completion`() {
        val access = EpisodeAccess.createDefault(id).withPlaybackProgress(90_000L, 123L, false)
        assertTrue(access.hasProgress)
        assertFalse(access.isViewed)
        assertEquals(123L, access.lastAccessRaw)
    }

    @Test fun `rewinding a viewed episode removes the completion mark`() {
        val access = EpisodeAccess(id, 1_400_000L, true, 1L).withPlaybackProgress(1000L, 2L, false)
        assertFalse(access.isViewed)
        assertEquals(1000L, access.seek)
    }

    @Test fun `manually viewed episode has no playback progress`() {
        val access = EpisodeAccess.createDefault(id).copy(isViewed = true)
        assertFalse(access.hasProgress)
        assertFalse(access.withPlaybackProgress(1000L, 2L, false).isViewed)
    }

    @Test fun `restarting a viewed episode removes its mark`() {
        val access = EpisodeAccess(id, 1_400_000L, true, 1L).withPlaybackProgress(0L, 2L, false)
        assertFalse(access.isViewed)
        assertFalse(access.hasProgress)
    }

    @Test fun `manual reset clears progress and completion`() {
        val reset = EpisodeAccess.createDefault(id)
        assertFalse(reset.hasProgress)
        assertFalse(reset.isViewed)
        assertEquals(0L, reset.lastAccessRaw)
    }

    @Test fun `opening a watched episode starts from zero`() {
        val started = EpisodeAccess(id, 1_400_000L, true, 1L).forPlaybackStart(2L)
        assertFalse(started.isViewed)
        assertEquals(0L, started.seek)
        assertEquals(2L, started.lastAccessRaw)
    }

    @Test fun `opening an unfinished episode resumes its position`() {
        val unfinished = EpisodeAccess(id, 90_000L, false, 1L)
        assertEquals(unfinished, unfinished.forPlaybackStart(2L))
    }

    @Test fun `explicit restart clears unfinished progress as well`() {
        val started = EpisodeAccess(id, 90_000L, false, 1L).forPlaybackStart(2L, restart = true)
        assertFalse(started.isViewed)
        assertEquals(0L, started.seek)
    }
}
