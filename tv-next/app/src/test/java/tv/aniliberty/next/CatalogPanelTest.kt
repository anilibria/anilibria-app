package tv.aniliberty.next

import android.widget.ScrollView
import androidx.activity.ComponentActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class CatalogPanelTest {
    @Test
    fun longDescriptionScrollsFromCloseButtonAndReturnsToItAtTheEnd() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val panel = TvSidePanel(controller.get(), "Описание")
        panel.message(List(150) { "Длинное описание, строка $it" }.joinToString("\n"))
        val close = panel.addAction("Закрыть") { panel.dismiss() }
        val other = panel.addAction("Другое") {}
        panel.show()
        val decor = panel.window!!.decorView
        decor.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(
                960,
                android.view.View.MeasureSpec.EXACTLY,
            ),
            android.view.View.MeasureSpec.makeMeasureSpec(
                540,
                android.view.View.MeasureSpec.EXACTLY,
            ),
        )
        decor.layout(0, 0, 960, 540)
        val scroll = panel.body.parent as ScrollView
        close.requestFocus()
        fun press(key: Int) {
            panel.dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, key))
            panel.dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, key))
        }
        press(android.view.KeyEvent.KEYCODE_DPAD_DOWN)
        assertTrue(scroll.scrollY > 0)
        scroll.scrollTo(0, Int.MAX_VALUE)
        val end = scroll.scrollY
        press(android.view.KeyEvent.KEYCODE_DPAD_DOWN)
        assertTrue(close.hasFocus())
        press(android.view.KeyEvent.KEYCODE_DPAD_DOWN)
        assertTrue(other.hasFocus())
        press(android.view.KeyEvent.KEYCODE_DPAD_UP)
        assertTrue(close.hasFocus())
        press(android.view.KeyEvent.KEYCODE_DPAD_UP)
        assertTrue(scroll.scrollY < end)
        assertTrue(panel.isShowing)
        panel.dismiss()
        controller.pause().stop().destroy()
    }

    @Test
    fun resetIsOnlyADraftAndCancelDoesNotApply() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller.get()
        var applied: CatalogFilter? = null
        val original = CatalogFilter(genres = setOf("15"), sorting = "RATING_DESC")
        val panel =
            CatalogFilterPanel(
                activity,
                Screen.Catalog(filters = original),
                mapOf("genres" to listOf(CatalogChoice("15", "Фэнтези"))),
            ) {
                applied = it
            }
        panel.show()
        panel.footer.getChildAt(1).performClick()
        assertNull(applied)
        panel.cancel()
        assertNull(applied)
        assertEquals(setOf("15"), original.genres)
        controller.pause().stop().destroy()
    }

    @Test
    fun genreSelectionUsesSamePanelAndBackCancelsUnconfirmedChanges() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        var applied: CatalogFilter? = null
        val panel =
            CatalogFilterPanel(
                controller.get(),
                Screen.Catalog(),
                mapOf("genres" to listOf(CatalogChoice("15", "Фэнтези"))),
            ) {
                applied = it
            }
        panel.show()
        panel.body.getChildAt(0).performClick()
        assertEquals("Жанры", panel.heading.text.toString())
        panel.body.getChildAt(0).performClick()
        panel.dispatchKeyEvent(
            android.view.KeyEvent(
                android.view.KeyEvent.ACTION_UP,
                android.view.KeyEvent.KEYCODE_BACK,
            )
        )
        assertEquals("Фильтры", panel.heading.text.toString())
        assertTrue(panel.isShowing)
        panel.footer.getChildAt(0).performClick()
        assertTrue(applied!!.genres.isEmpty())
        controller.pause().stop().destroy()
    }

    @Test
    fun applyingAfterResetPreservesSortAndClearsGenreDeepLink() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        var applied: CatalogFilter? = null
        val panel =
            CatalogFilterPanel(
                controller.get(),
                Screen.Catalog(genre = 15, filters = CatalogFilter(sorting = "YEAR_DESC")),
                emptyMap(),
            ) {
                applied = it
            }
        panel.show()
        assertTrue(panel.body.getChildAt(0).isFocusable)
        panel.footer.getChildAt(1).performClick()
        panel.footer.getChildAt(0).performClick()
        assertNotNull(applied)
        assertTrue(applied!!.genres.isEmpty())
        assertEquals("YEAR_DESC", applied!!.sorting)
        assertFalse(panel.isShowing)
        controller.pause().stop().destroy()
    }
}
