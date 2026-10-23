package tv.aniliberty.next

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class FontTest {
    @Test
    fun bundledFontsHaveExplicitReadableWeightsInsteadOfVariableThinDefaults() {
        for ((id, weight) in listOf(R.font.inter to 400, R.font.montserrat to 600)) {
            val bytes =
                RuntimeEnvironment.getApplication().resources.openRawResource(id).use {
                    it.readBytes()
                }
            val data = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
            val count = data.getShort(4).toInt() and 65535
            val tables =
                (0 until count).associate { index ->
                    val at = 12 + index * 16
                    String(bytes, at, 4, Charsets.US_ASCII) to data.getInt(at + 8)
                }
            assertFalse(tables.containsKey("fvar"))
            assertEquals(weight, data.getShort(tables.getValue("OS/2") + 4).toInt() and 65535)
        }
    }
}
