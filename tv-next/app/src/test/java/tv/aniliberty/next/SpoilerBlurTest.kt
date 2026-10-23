package tv.aniliberty.next

import android.graphics.Bitmap
import android.graphics.Color
import coil3.size.Size
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpoilerBlurTest {
    @Test
    fun blursEdgesWithoutModifyingOriginalAndBoundsBitmapMemory() = runBlocking {
        val original = Bitmap.createBitmap(192, 108, Bitmap.Config.ARGB_8888)
        for (y in 0 until 108) for (x in 0 until 192) original.setPixel(
            x,
            y,
            if (x < 96) Color.BLACK else Color.WHITE,
        )
        val result = SpoilerBlurTransformation().transform(original, Size.ORIGINAL)
        assertTrue(result.width <= 96 && result.height <= 96)
        val middle = Color.red(result.getPixel(result.width / 2, result.height / 2))
        assertTrue(middle in 1..254)
        assertEquals(Color.BLACK, original.getPixel(95, 54))
        assertFalse(original.isRecycled)
    }
}
