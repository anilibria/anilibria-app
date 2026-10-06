package ru.radiationx.anilibria.screen.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSkipPlaybackStateTest {
    private val state = PlayerSkipPlaybackState()

    @Test
    fun `ready without a rendered frame does not show prompts`() {
        state.update(isReady = true, isPlaying = true)
        assertFalse(state.canShow)
        state.frameRendered()
        assertTrue(state.canShow)
    }

    @Test
    fun `frame rendered while buffering waits for readiness`() {
        state.frameRendered()
        assertFalse(state.canShow)
        state.update(isReady = true, isPlaying = true)
        assertTrue(state.canShow)
    }

    @Test
    fun `seek invalidates the previous frame even in a ready buffer`() {
        state.update(isReady = true, isPlaying = true)
        state.frameRendered()
        state.framePending()
        assertFalse(state.canShow)
        state.frameRendered()
        assertTrue(state.canShow)
    }

    @Test
    fun `seeking repeatedly to zero keeps its already rendered frame`() {
        state.update(isReady = true, isPlaying = false)
        state.frameRendered()
        repeat(10) { state.framePending(positionChanged = false) }
        assertTrue(state.canShow)
    }

    @Test
    fun `pause keeps the prompt available but freezes countdown`() {
        state.frameRendered()
        state.update(isReady = true, isPlaying = false)
        assertTrue(state.canShow)
        assertFalse(state.canCount(appearanceFinished = true, watchFocused = false))
    }

    @Test
    fun `buffering hides the prompt and stops countdown`() {
        state.frameRendered()
        state.update(isReady = false, isPlaying = false)
        assertFalse(state.canShow)
        assertFalse(state.canCount(appearanceFinished = true, watchFocused = false))
    }

    @Test
    fun `animation and Watch focus each prevent countdown`() {
        state.frameRendered()
        state.update(isReady = true, isPlaying = true)
        assertFalse(state.canCount(appearanceFinished = false, watchFocused = false))
        assertFalse(state.canCount(appearanceFinished = true, watchFocused = true))
        assertTrue(state.canCount(appearanceFinished = true, watchFocused = false))
    }
}
