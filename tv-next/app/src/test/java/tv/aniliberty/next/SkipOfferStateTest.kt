package tv.aniliberty.next

import org.junit.Assert.*
import org.junit.Test

class SkipOfferStateTest {
    @Test
    fun offerPersistsAcrossUpdatesAndTimelineVisibility() {
        val state = SkipOfferState()
        repeat(1000) { assertTrue(state.visible("ep:opening", true)) }
        assertFalse(state.visible("ep:opening", false))
        assertTrue(state.visible("ep:opening", true))
        assertFalse(state.visible(null, true))
    }

    @Test
    fun watchDismissesCurrentRangeButNotEndingOrNextEpisode() {
        val state = SkipOfferState()
        state.dismiss("ep:opening")
        assertFalse(state.visible("ep:opening", true))
        assertTrue(state.dismissed("ep:opening"))
        assertTrue(state.visible("ep:ending", true))
        assertTrue(state.visible("next:opening", true))
    }
}
