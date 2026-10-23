package tv.aniliberty.next

import org.junit.Assert.*
import org.junit.Test

class HomeNavigationTest {
    private val rows =
        listOf(
            Row("latest-title", Kind.HEADING),
            Row("latest", Kind.SHELF),
            Row("continue-title", Kind.HEADING),
            Row("continue", Kind.SHELF),
            Row("days", Kind.DAYS),
            Row("schedule", Kind.SHELF),
        )

    @Test
    fun movesToAdjacentContentAndSkipsHeadings() {
        assertEquals(1, homeFocusDestination(rows, 0, 1))
        assertEquals(3, homeFocusDestination(rows, 1, 1))
        assertEquals(1, homeFocusDestination(rows, 3, -1))
        assertEquals(4, homeFocusDestination(rows, 3, 1))
        assertEquals(5, homeFocusDestination(rows, 4, 1))
        assertNull(homeFocusDestination(rows, 0, -1))
        assertNull(homeFocusDestination(rows, 5, 1))
    }

    @Test
    fun visibleBlocksDoNotTriggerExtraScroll() {
        assertEquals(0, homeScrollDelta(200, 500, 14, 700))
        assertEquals(-54, homeScrollDelta(-40, 300, 14, 700))
        assertEquals(100, homeScrollDelta(300, 800, 14, 700))
        assertEquals(86, homeScrollDelta(100, 900, 14, 700))
    }
}
