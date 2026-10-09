package ru.radiationx.anilibria.screen.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.radiationx.data.entity.domain.release.PlayerSkips

class PlayerTimelineSegmentTest {
    @Test
    fun `opening and ending retain millisecond ranges and their types`() {
        val segments = PlayerSkips(skip(90_500, 180_500), skip(1_320_000, 1_410_000))
            .timelineSegments(1_440_000)
        assertEquals(listOf(
            PlayerTimelineSegment(PlayerTimelineSegment.Type.OPENING, 90_500, 180_500),
            PlayerTimelineSegment(PlayerTimelineSegment.Type.ENDING, 1_320_000, 1_410_000),
        ), segments)
    }

    @Test
    fun `missing markers and unknown duration do not produce segments`() {
        assertTrue((null as PlayerSkips?).timelineSegments(1_000).isEmpty())
        assertTrue(PlayerSkips(null, null).timelineSegments(1_000).isEmpty())
        val skips = PlayerSkips(skip(0, 500), null)
        listOf(0L, -1L, Long.MIN_VALUE + 1).forEach {
            assertTrue(skips.timelineSegments(it).isEmpty())
        }
    }

    @Test
    fun `each optional marker works on its own`() {
        assertEquals(PlayerTimelineSegment.Type.OPENING,
            PlayerSkips(skip(0, 500), null).timelineSegments(1_000).single().type)
        assertEquals(PlayerTimelineSegment.Type.ENDING,
            PlayerSkips(null, skip(500, 1_000)).timelineSegments(1_000).single().type)
    }

    @Test
    fun `invalid ranges are ignored without hiding the valid marker`() {
        listOf(skip(-1, 500), skip(0, 0), skip(500, 100), skip(1_000, 2_000))
            .forEach { invalid ->
                val segments = PlayerSkips(invalid, skip(500, 1_000)).timelineSegments(1_000)
                assertEquals(1, segments.size)
                assertEquals(PlayerTimelineSegment.Type.ENDING, segments.single().type)
            }
    }

    @Test
    fun `range past video end is clipped including extremely large timestamps`() {
        assertEquals(1_000L,
            PlayerSkips(null, skip(500, Long.MAX_VALUE)).timelineSegments(1_000).single().end)
    }

    @Test
    fun `overlapping and reversed marker order keep correct identities`() {
        val segments = PlayerSkips(skip(500, 900), skip(100, 700)).timelineSegments(1_000)
        assertEquals(listOf(PlayerTimelineSegment.Type.ENDING, PlayerTimelineSegment.Type.OPENING),
            segments.map { it.type })
    }

    private fun skip(start: Long, end: Long) = PlayerSkips.Skip(start, end)
}
