package tv.aniliberty.next

import android.content.Context
import android.os.Looper
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
class CatalogLifecycleTest {
    @Test
    fun catalogueAppendFilterEmptyAndHomeReturnKeepValidGridLayout() {
        RuntimeEnvironment.getApplication()
            .getSharedPreferences("viewing", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup().visible()
        val activity = controller.get()
        shadowOf(Looper.getMainLooper()).idle()
        val model =
            MainActivity::class
                .java
                .getDeclaredMethod("getModel")
                .apply { isAccessible = true }
                .invoke(activity) as TvModel
        val root = activity.window.decorView
        fun render(state: UiState) {
            model.state.value = state
            shadowOf(Looper.getMainLooper()).idle()
            root.measure(
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            )
            root.layout(0, 0, 1920, 1080)
            shadowOf(Looper.getMainLooper()).idle()
        }
        val releases =
            (1..40).map {
                parseRelease(
                    JSONObject()
                        .put("id", it)
                        .put("name", JSONObject().put("main", "Название $it"))
                        .put("episodes_total", 434)
                )
            }
        render(UiState(screen = Screen.Catalog(), releases = releases.take(30), more = true))
        render(UiState(screen = Screen.Catalog(), releases = releases, more = false))
        render(UiState(screen = Screen.Catalog(query = "Нет совпадений")))
        render(
            UiState(
                screen = Screen.Home,
                home =
                    HomeData(
                        emptyList(),
                        listOf(emptyList(), emptyList(), emptyList()),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                    ),
            )
        )
        render(UiState(screen = Screen.Catalog(), releases = releases.take(4)))
        val list =
            MainActivity::class
                .java
                .getDeclaredField("list")
                .apply { isAccessible = true }
                .get(activity) as RecyclerView
        val grid = list.layoutManager as androidx.recyclerview.widget.GridLayoutManager
        assertEquals(20, grid.spanCount)
        val adapter = list.adapter as PageAdapter
        val first = adapter.rows.indexOfFirst { it.kind == Kind.POSTER }
        assertEquals(5, grid.spanSizeLookup.getSpanSize(first))
        assertEquals(20, grid.spanSizeLookup.getSpanSize(0))
        // TV crash: GridLayoutManager can query NO_POSITION while re-anchoring.
        assertEquals(20, grid.spanSizeLookup.getSpanSize(RecyclerView.NO_POSITION))
        assertEquals(20, grid.spanSizeLookup.getSpanSize(adapter.itemCount))
        controller.pause().stop().destroy()
    }
}
