package tv.aniliberty.next

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], application = Application::class)
class BrandAssetsTest {
    @Test
    fun convertedBrandAndMenuVectorsLoadWithCorrectDimensions() {
        val context = RuntimeEnvironment.getApplication()
        listOf(
                R.drawable.ic_similar,
                R.mipmap.ic_launcher,
                R.drawable.tv_banner,
                R.drawable.ic_aniliberty,
                R.drawable.ic_search,
                R.drawable.ic_home,
                R.drawable.ic_catalog,
                R.drawable.ic_library,
                R.drawable.ic_refresh,
                R.drawable.ic_arrow_right,
                R.drawable.ic_play,
                R.drawable.ic_pause,
                R.drawable.ic_previous,
                R.drawable.ic_next,
            )
            .forEach { id ->
                val drawable = context.getDrawable(id)!!
                assertTrue(drawable.intrinsicWidth > 0)
                assertTrue(drawable.intrinsicHeight > 0)
                drawable.setBounds(0, 0, 64, 64)
                drawable.draw(Canvas(Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)))
            }
    }
}
