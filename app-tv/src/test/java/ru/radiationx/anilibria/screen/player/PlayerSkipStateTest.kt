package ru.radiationx.anilibria.screen.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import ru.radiationx.data.entity.domain.release.PlayerSkips

class PlayerSkipStateTest {
    private val opening = PlayerSkips.Skip(30_000, 120_000)
    private val ending = PlayerSkips.Skip(1_300_000, 1_400_000)
    private val state = PlayerSkipState().apply { reset(PlayerSkips(opening, ending)) }

    @Test
    fun `normal playback still offers opening and ending skips`() {
        state.update(29_999)
        assertNull(state.currentSkip)
        state.update(30_000)
        assertEquals(opening, state.currentSkip)
        state.update(120_001)
        assertNull(state.currentSkip)
        state.update(1_300_000)
        assertEquals(ending, state.currentSkip)
    }

    @Test
    fun `preview positions never show skip prompts`() {
        state.startSeek()
        listOf(30_000L, 60_000L, 120_000L, 1_350_000L).forEach {
            state.update(it)
            assertNull(state.currentSkip)
        }
    }

    @Test
    fun `confirmed seek into opening offers its skip prompt`() {
        state.startSeek()
        state.update(60_000)
        state.finishSeek(60_000, cancelled = false)
        assertEquals(opening, state.currentSkip)
        state.update(65_000)
        assertEquals(opening, state.currentSkip)
        state.update(1_350_000)
        assertEquals(ending, state.currentSkip)
    }

    @Test
    fun `confirmed seek into ending offers its skip prompt`() {
        state.startSeek()
        state.finishSeek(1_350_000, cancelled = false)
        assertEquals(ending, state.currentSkip)
        state.update(1_360_000)
        assertEquals(ending, state.currentSkip)
    }

    @Test
    fun `confirmed backward seek restores a previously watched opening`() {
        state.update(150_000)
        state.startSeek()
        state.update(60_000)
        assertNull(state.currentSkip)
        state.finishSeek(60_000, cancelled = false)
        assertEquals(opening, state.currentSkip)
    }

    @Test
    fun `cancelled seek preserves an explicitly dismissed opening`() {
        state.update(40_000)
        state.dismissCurrentSkip()
        state.startSeek()
        state.update(60_000)
        state.finishSeek(40_000, cancelled = true)
        assertNull(state.currentSkip)
    }

    @Test
    fun `cancelled preview beyond ending preserves the future opening prompt`() {
        state.update(10_000)
        state.startSeek()
        state.update(1_450_000)
        state.finishSeek(10_000, cancelled = true)
        state.update(40_000)
        assertEquals(opening, state.currentSkip)
    }

    @Test
    fun `cancelled seek restores an already active opening prompt`() {
        state.update(40_000)
        state.startSeek()
        state.update(1_350_000)
        state.finishSeek(40_000, cancelled = true)
        assertEquals(opening, state.currentSkip)
    }

    @Test
    fun `confirmed seek before opening preserves natural skip detection`() {
        state.startSeek()
        state.update(1_350_000)
        state.finishSeek(20_000, cancelled = false)
        state.update(40_000)
        assertEquals(opening, state.currentSkip)
    }

    @Test
    fun `changing episodes resets dismissed skips and seek state`() {
        state.update(40_000)
        state.dismissCurrentSkip()
        state.startSeek()
        state.reset(PlayerSkips(opening, ending))
        assertFalse(state.isSeeking)
        state.update(40_000)
        assertEquals(opening, state.currentSkip)
    }
}
