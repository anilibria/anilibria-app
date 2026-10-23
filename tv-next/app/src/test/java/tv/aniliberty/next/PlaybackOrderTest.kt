package tv.aniliberty.next

import org.junit.Assert.*
import org.junit.Test

class PlaybackOrderTest {
    private fun episode(id: String, ordinal: Double, order: Int) =
        Episode(id, ordinal, "", null, 0, emptyList(), order.toDouble())

    @Test
    fun resumeAdvancesOnlyAfterCompletionAndKeepsServerOrder() {
        val episodes =
            listOf(episode("a", 1.0, 0), episode("special", 1.5, 1), episode("last", 3.0, 2))
        assertEquals("a", resumeEpisode(episodes, "a") { 500L to 2000L }?.id)
        assertEquals("special", resumeEpisode(episodes, "a") { 2000L to 2000L }?.id)
        assertEquals("last", resumeEpisode(episodes, "last") { 2000L to 2000L }?.id)
        assertEquals("a", resumeEpisode(episodes, "missing") { 0L to 0L }?.id)
        assertNull(resumeEpisode(emptyList(), null) { 0L to 0L })
    }

    @Test
    fun resumeUsesConfiguredThreshold() {
        val episodes = listOf(episode("a", 1.0, 0), episode("b", 2.0, 1))
        assertEquals("b", resumeEpisode(episodes, "a", 85) { 850L to 1000L }?.id)
        assertEquals("a", resumeEpisode(episodes, "a", 95) { 850L to 1000L }?.id)
    }

    @Test
    fun nextUsesServerOrderIncludingFractionalAndMissingOrdinals() {
        val episodes =
            listOf(episode("a", 1.0, 0), episode("special", 12.5, 1), episode("last", 20.0, 2))
        assertEquals("special", nextEpisode(episodes, "a")?.id)
        assertEquals("last", nextEpisode(episodes, "special")?.id)
    }

    @Test
    fun missingCurrentOrLastNeverWrapToFirst() {
        val episodes = listOf(episode("a", 1.0, 0))
        assertNull(nextEpisode(episodes, "a"))
        assertNull(nextEpisode(episodes, "removed"))
        assertNull(nextEpisode(emptyList(), null))
    }
}
