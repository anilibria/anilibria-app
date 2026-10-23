package tv.aniliberty.next

import android.content.Context
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class RemoteLifecycleTest {
    @Test
    fun rapidGridMovesShortRowRefreshAndMenuReturnKeepLogicalFocus() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("viewing", Context.MODE_PRIVATE).edit().clear().commit()
        context
            .getSharedPreferences("account-session", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        val controller =
            Robolectric.buildActivity(MainActivity::class.java)
                .setup()
                .visible()
                .windowFocusChanged(true)
        val activity = controller.get()
        val model =
            MainActivity::class
                .java
                .getDeclaredMethod("getModel")
                .apply { isAccessible = true }
                .invoke(activity) as TvModel
        val list =
            MainActivity::class
                .java
                .getDeclaredField("list")
                .apply { isAccessible = true }
                .get(activity) as RecyclerView
        val releases = (1..10).map { parseRelease(JSONObject().put("id", it)) }
        fun layout() {
            shadowOf(Looper.getMainLooper()).idle()
            val root = activity.window.decorView
            root.measure(
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            )
            root.layout(0, 0, 1920, 1080)
            shadowOf(Looper.getMainLooper()).idle()
        }
        fun key(code: Int) {
            activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
            activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
            layout()
        }
        model.state.value = UiState(Screen.Catalog(), releases = releases)
        layout()
        list
            .findViewHolderForAdapterPosition(
                (list.adapter as PageAdapter).rows.indexOfFirst { it.release?.id == 1 }
            )!!
            .itemView
            .requestFocus()
        repeat(3) { key(KeyEvent.KEYCODE_DPAD_RIGHT) }
        assertEquals("release:4", list.findFocus()?.tag)
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        assertEquals("release:8", list.findFocus()?.tag)
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        assertEquals("release:10", list.findFocus()?.tag)
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertEquals("release:10", list.findFocus()?.tag)
        model.state.value = model.state.value.copy(loadingMore = true)
        layout()
        assertEquals("release:10", list.findFocus()?.tag)
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        assertEquals("release:9", list.findFocus()?.tag)
        key(KeyEvent.KEYCODE_DPAD_LEFT)
        val menu =
            MainActivity::class
                .java
                .getDeclaredField("menu")
                .apply { isAccessible = true }
                .get(activity) as android.widget.LinearLayout
        assertEquals("menu-route:1", menu.findFocus()?.tag)
        key(KeyEvent.KEYCODE_DPAD_DOWN)
        assertEquals("menu-route:9", menu.findFocus()?.tag)
        key(KeyEvent.KEYCODE_DPAD_UP)
        assertEquals("menu-route:1", menu.findFocus()?.tag)
        key(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertEquals("release:9", list.findFocus()?.tag)
        controller.pause().stop().destroy()
    }
}
