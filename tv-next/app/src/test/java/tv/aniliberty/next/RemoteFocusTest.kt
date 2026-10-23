package tv.aniliberty.next

import android.view.View
import org.junit.Assert.*
import org.junit.Test

class RemoteFocusTest {
    @Test
    fun fourColumnGridPreservesColumnAndClampsLastRow() {
        val spans = listOf(20, 20) + List(10) { 5 }
        val enabled = listOf(false, true) + List(10) { true }
        assertEquals(9, remoteGridTarget(spans, enabled, 20, 5, View.FOCUS_DOWN))
        assertEquals(11, remoteGridTarget(spans, enabled, 20, 9, View.FOCUS_DOWN))
        assertNull(remoteGridTarget(spans, enabled, 20, 5, View.FOCUS_RIGHT))
        assertNull(remoteGridTarget(spans, enabled, 20, 2, View.FOCUS_LEFT))
        assertEquals(1, remoteGridTarget(spans, enabled, 20, 2, View.FOCUS_UP))
    }

    @Test
    fun skipsDecorativeHeadingsAndProfileButFindsRefreshAndToolbar() {
        val spans = listOf(20, 20, 20, 10, 10, 20)
        val enabled = listOf(true, false, false, true, true, true)
        assertEquals(3, remoteGridTarget(spans, enabled, 20, 0, View.FOCUS_DOWN))
        assertEquals(0, remoteGridTarget(spans, enabled, 20, 4, View.FOCUS_UP))
        assertEquals(5, remoteGridTarget(spans, enabled, 20, 4, View.FOCUS_DOWN))
        assertEquals(4, remoteGridTarget(spans, enabled, 20, 3, View.FOCUS_RIGHT))
    }

    @Test
    fun shelvesAndBarsAreOneVerticalStopWithoutHorizontalEscape() {
        val spans = List(4) { 20 }
        val enabled = listOf(true, false, true, true)
        assertEquals(2, remoteGridTarget(spans, enabled, 20, 0, View.FOCUS_DOWN))
        assertEquals(0, remoteGridTarget(spans, enabled, 20, 2, View.FOCUS_UP))
        assertNull(remoteGridTarget(spans, enabled, 20, 2, View.FOCUS_RIGHT))
        assertNull(remoteGridTarget(spans, enabled, 20, -1, View.FOCUS_DOWN))
    }
}
