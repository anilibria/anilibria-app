package ru.radiationx.anilibria.screen.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeWatchPolicyTest {
    private val duration = 24 * 60 * 1000L

    @Test fun `85 percent boundary is inclusive`() {
        val threshold = duration * 85 / 100
        assertFalse(EpisodeWatchPolicy.isViewed(threshold - 1L, duration))
        assertTrue(EpisodeWatchPolicy.isViewed(threshold, duration))
    }

    @Test fun `last five minutes alone do not count as viewed`() {
        assertFalse(EpisodeWatchPolicy.isViewed(duration - 300_000L, duration))
    }

    @Test fun `short episodes use the same percentage`() {
        assertFalse(EpisodeWatchPolicy.isViewed(101_999L, 120_000L))
        assertTrue(EpisodeWatchPolicy.isViewed(102_000L, 120_000L))
    }

    @Test fun `natural completion always marks viewed`() {
        assertTrue(EpisodeWatchPolicy.isViewed(0L, 0L, ended = true))
        assertTrue(EpisodeWatchPolicy.isViewed(duration, duration))
    }

    @Test fun `unknown duration does not count as completion`() {
        assertFalse(EpisodeWatchPolicy.isViewed(1000L, 0L))
        assertFalse(EpisodeWatchPolicy.isViewed(1000L, Long.MIN_VALUE))
    }

    @Test fun `rewinding below threshold removes completion`() {
        assertTrue(EpisodeWatchPolicy.isViewed(duration - 1L, duration))
        assertFalse(EpisodeWatchPolicy.isViewed(1000L, duration))
    }

    @Test fun `zero and unset positions do not count as playback`() {
        assertFalse(EpisodeWatchPolicy.isViewed(0L, 120_000L))
        assertFalse(EpisodeWatchPolicy.isViewed(-1L, duration))
        assertFalse(EpisodeWatchPolicy.isViewed(1L, 120_000L))
    }

    @Test fun `ending start takes priority over 85 percent`() {
        val endingStart = 22 * 60 * 1000L
        assertFalse(EpisodeWatchPolicy.isViewed(duration * 85 / 100, duration, endingStart))
        assertFalse(EpisodeWatchPolicy.isViewed(endingStart - 1L, duration, endingStart))
        assertTrue(EpisodeWatchPolicy.isViewed(endingStart, duration, endingStart))
        assertTrue(EpisodeWatchPolicy.isViewed(duration - 1L, duration, endingStart))
    }

    @Test fun `early ending start counts before 85 percent`() {
        val endingStart = 20 * 60 * 1000L
        assertTrue(EpisodeWatchPolicy.isViewed(endingStart, duration, endingStart))
    }

    @Test fun `rewinding before ending removes completion`() {
        val endingStart = 22 * 60 * 1000L
        assertTrue(EpisodeWatchPolicy.isViewed(endingStart + 1000L, duration, endingStart))
        assertFalse(EpisodeWatchPolicy.isViewed(endingStart - 1000L, duration, endingStart))
    }

    @Test fun `invalid ending start uses 85 percent`() {
        listOf(-1L, 0L, duration, duration + 1L).forEach {
            assertFalse(EpisodeWatchPolicy.isViewed(duration * 85 / 100 - 1L, duration, it))
            assertTrue(EpisodeWatchPolicy.isViewed(duration * 85 / 100, duration, it))
        }
    }

    @Test fun `ending start works without known duration or ending end`() {
        assertFalse(EpisodeWatchPolicy.isViewed(1000L, 0L, 2000L))
        assertTrue(EpisodeWatchPolicy.isViewed(2000L, 0L, 2000L))
        assertTrue(EpisodeWatchPolicy.isViewed(3000L, 0L, 2000L))
    }
}
