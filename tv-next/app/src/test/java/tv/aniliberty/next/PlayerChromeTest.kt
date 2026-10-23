package tv.aniliberty.next

import android.view.View
import android.widget.FrameLayout
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PlayerChromeTest {
    @Test
    fun timelineShowsRemainingAndHoursWithoutInventingDuration() {
        val chrome = PlayerChrome(RuntimeEnvironment.getApplication())
        chrome.update(90000, 120000)
        assertEquals("1:30", chrome.elapsed.text.toString())
        assertEquals(750, chrome.seek.progress)
        assertSame(chrome.buttons, chrome.getChildAt(chrome.childCount - 1))
        assertTrue(chrome.remaining.text.contains("0:30"))
        assertEquals("1:01:01", playbackTime(3661000))
        chrome.update(0, 0)
        assertFalse(chrome.seek.isEnabled)
        assertSame(chrome.buttons, chrome.getChildAt(chrome.childCount - 1))
    }

    @Test
    fun coreTransportFitsOneRowAndHasAccessibleIconLabels() {
        val chrome = PlayerChrome(RuntimeEnvironment.getApplication())
        chrome.icon("Предыдущая серия", R.drawable.ic_previous) {}
        chrome.icon("Пауза", R.drawable.ic_pause, true) {}
        chrome.icon("Следующая серия", R.drawable.ic_next) {}
        chrome.button("Серии", R.drawable.ic_episodes) {}
        chrome.button("Настройки", R.drawable.ic_settings) {}
        chrome.button("Пропустить опенинг", R.drawable.ic_next) {}
        chrome.measure(
            View.MeasureSpec.makeMeasureSpec(960, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        chrome.layout(0, 0, 960, chrome.measuredHeight)
        assertTrue(chrome.buttons.getChildAt(5).right <= chrome.buttons.width)
        assertEquals("Пауза", chrome.buttons.getChildAt(1).contentDescription)
        assertTrue(
            "Control height: ${chrome.measuredHeight}px",
            chrome.measuredHeight < chrome.dp(300),
        )
    }

    @Test
    fun episodeAdapterUsesLazyRowsAndPreservesRealEpisodeLabels() {
        val episode =
            parseRelease(
                    JSONObject(
                        """{"id":1,"episodes":[{"id":"e","ordinal":3,"name":"Название","hls_720":"https://example.test/e.m3u8"}]}"""
                    )
                )
                .episodes
                .single()
        val adapter =
            PlayerEpisodeAdapter(
                List(1000) { episode.copy(id = "e:$it") },
                "e:20",
                { 0L to 0L },
                {},
            )
        assertEquals(1000, adapter.itemCount)
        val holder = adapter.onCreateViewHolder(FrameLayout(RuntimeEnvironment.getApplication()), 0)
        adapter.onBindViewHolder(holder, 20)
        assertEquals(episode.label, holder.name.text.toString())
        assertTrue(holder.status.text.contains("Сейчас"))
        assertTrue(holder.box.isFocusable)
    }

    @Test
    fun skipActionIsAboveTimelineAndOutsideTransportRow() {
        val chrome = PlayerChrome(RuntimeEnvironment.getApplication())
        val skip = action(chrome.skipActions, "Пропустить опенинг") {}
        chrome.skipActions.addView(
            skip,
            android.widget.LinearLayout.LayoutParams(-2, chrome.dp(48)),
        )
        chrome.skipActions.visibility = View.VISIBLE
        chrome.measure(
            View.MeasureSpec.makeMeasureSpec(960, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        chrome.layout(0, 0, 960, chrome.measuredHeight)
        assertTrue(chrome.skipActions.bottom <= chrome.seek.top)
        assertEquals("Пропустить опенинг", skip.text.toString())
        assertTrue(skip.width <= chrome.skipActions.width)
        assertEquals(0, chrome.buttons.childCount)
        assertEquals(1, chrome.skipActions.childCount)
    }
}
