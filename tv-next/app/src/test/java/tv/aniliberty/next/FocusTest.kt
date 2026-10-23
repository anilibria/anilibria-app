package tv.aniliberty.next

import android.app.Application
import android.view.KeyEvent
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], application = Application::class)
class FocusTest {
    @Test
    fun tabsEnterContentOnOkOrDownButNotOnFocus() {
        var selected = -1
        val adapter = PageAdapter({ selected = it }, {}, {}, {}, { 0L to 0L }, { null }, {})
        adapter.submit(listOf(Row("tabs", Kind.TABS)))
        val parent = FrameLayout(RuntimeEnvironment.getApplication())
        val holder = adapter.onCreateViewHolder(parent, Kind.TABS.ordinal)
        adapter.onBindViewHolder(holder, 0)
        val line = (holder.itemView as HorizontalScrollView).getChildAt(0) as LinearLayout
        val tab = (line.getChildAt(3) as LinearLayout).getChildAt(0)
        tab.requestFocus()
        assertEquals(-1, selected)
        tab.performClick()
        assertEquals(3, selected)
        selected = -1
        tab.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(-1, selected)
        tab.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(3, selected)
    }

    @Test
    fun posterKeepsReadableCaptionAndGeometryWhenFocused() {
        val adapter = PageAdapter({}, {}, {}, {}, { 0L to 0L }, { null }, {})
        val release =
            parseRelease(
                org.json.JSONObject(
                    """{"id":10,"name":{"main":"Тайтл"},"year":2026,"latest_episode":{"id":"episode-3","ordinal":3}}"""
                )
            )
        adapter.submit(listOf(Row("release:10", Kind.POSTER, release = release)))
        val holder =
            adapter.onCreateViewHolder(
                FrameLayout(RuntimeEnvironment.getApplication()),
                Kind.POSTER.ordinal,
            )
        adapter.onBindViewHolder(holder, 0)
        val box = holder.itemView as FrameLayout
        val content = box.getChildAt(2) as LinearLayout
        val metadata = content.getChildAt(1) as android.widget.TextView
        val height = box.layoutParams.height
        assertEquals(android.view.View.VISIBLE, content.getChildAt(0).visibility)
        assertEquals("Тайтл", (content.getChildAt(0) as android.widget.TextView).text.toString())
        assertEquals(android.view.View.GONE, metadata.visibility)
        box.requestFocus()
        assertEquals(android.view.View.VISIBLE, metadata.visibility)
        assertTrue(metadata.text.contains("2026"))
        assertEquals(height, box.layoutParams.height)
    }

    @Test
    fun longOkOpensActionsForTheExactRelease() {
        var selected: Int? = null
        val adapter =
            PageAdapter(
                {},
                {},
                {},
                {},
                { 0L to 0L },
                { null },
                {},
                quickActions = { selected = it.id },
            )
        val release = parseRelease(org.json.JSONObject().put("id", 22))
        adapter.submit(listOf(Row("release:22", Kind.POSTER, release = release)))
        val holder =
            adapter.onCreateViewHolder(
                FrameLayout(RuntimeEnvironment.getApplication()),
                Kind.POSTER.ordinal,
            )
        adapter.onBindViewHolder(holder, 0)
        assertTrue(holder.itemView.performLongClick())
        assertEquals(22, selected)
    }

    @Test
    fun shelfUsesExactReleaseAndKeepsHorizontalLayout() {
        var opened: Int? = null
        val adapter = PageAdapter({}, {}, {}, { opened = it.id }, { 0L to 0L }, { null }, {})
        val release = parseRelease(org.json.JSONObject().put("id", 87))
        adapter.submit(listOf(Row("latest", Kind.SHELF, items = listOf(release))))
        val holder =
            adapter.onCreateViewHolder(
                FrameLayout(RuntimeEnvironment.getApplication()),
                Kind.SHELF.ordinal,
            )
        adapter.onBindViewHolder(holder, 0)
        val shelf = holder.itemView as androidx.recyclerview.widget.RecyclerView
        assertEquals(
            androidx.recyclerview.widget.RecyclerView.HORIZONTAL,
            (shelf.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).orientation,
        )
        val cards = shelf.adapter as PageAdapter
        val card = cards.onCreateViewHolder(shelf, Kind.POSTER.ordinal)
        cards.onBindViewHolder(card, 0)
        card.itemView.performClick()
        assertEquals(87, opened)
        assertTrue(card.itemView.layoutParams.width > 0)
    }

    @Test
    fun headingsDoNotConsumeRemoteFocus() {
        val adapter = PageAdapter({}, {}, {}, {}, { 0L to 0L }, { null }, {})
        adapter.submit(listOf(Row("heading", Kind.HEADING, "Новые эпизоды")))
        val holder =
            adapter.onCreateViewHolder(
                FrameLayout(RuntimeEnvironment.getApplication()),
                Kind.HEADING.ordinal,
            )
        adapter.onBindViewHolder(holder, 0)
        assertFalse(holder.itemView.isFocusable)
        assertFalse(holder.itemView.isClickable)
    }

    @Test
    fun homeDayButtonsKeepExactViewAndFocusAcrossRefresh() {
        val adapter = PageAdapter({}, {}, {}, {}, { 0L to 0L }, { null }, {})
        val row = Row("schedule-days", Kind.DAYS, "Расписание релизов", selected = 1)
        adapter.submit(listOf(row))
        val holder =
            adapter.onCreateViewHolder(
                FrameLayout(RuntimeEnvironment.getApplication()),
                Kind.DAYS.ordinal,
            )
        adapter.onBindViewHolder(holder, 0)
        val line = (holder.itemView as HorizontalScrollView).getChildAt(0) as LinearLayout
        val tomorrow = line.getChildAt(3)
        tomorrow.requestFocus()
        adapter.submit(listOf(row.copy(selected = 2)))
        adapter.onBindViewHolder(holder, 0)
        assertSame(tomorrow, line.getChildAt(3))
        assertTrue(tomorrow.hasFocus())
    }
}
