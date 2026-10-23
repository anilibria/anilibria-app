package tv.aniliberty.next

import org.junit.Assert.*
import org.junit.Test

class PanelPlaybackTest {
    @Test
    fun nestedSettingsPauseOnceAndClosingResumesOnce() {
        val state = PanelPlayback()
        var paused = 0
        var played = 0
        repeat(4) { state.open { paused++ } }
        assertEquals(1, paused)
        assertEquals(0, played)
        state.close { played++ }
        state.close { played++ }
        assertEquals(1, played)
        state.open { paused++ }
        state.close { played++ }
        assertEquals(2, paused)
        assertEquals(2, played)
    }

    @Test
    fun selectingAnotherEpisodeDoesNotResumeOldStream() {
        val state = PanelPlayback()
        var played = 0
        state.open {}
        state.close(false) { played++ }
        state.close { played++ }
        assertEquals(0, played)
    }
}
