package ru.radiationx.anilibria.screen.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerBufferPolicyTest {
    private val mib = 1024 * 1024L

    @Test
    fun `small heaps receive smaller buffers and the tested TV retains its budget`() {
        val profiles = listOf(64 to 8, 128 to 16, 192 to 24, 256 to 32, 384 to 48, 512 to 64)
        profiles.forEach { (heapMiB, bufferMiB) ->
            assertEquals("Heap $heapMiB MiB", bufferMiB * mib, PlayerBufferPolicy.targetBufferBytes(heapMiB * mib).toLong())
        }
    }

    @Test
    fun `large heaps stay capped without overflowing the Media3 integer budget`() {
        listOf(1024 * mib, 4096 * mib, Long.MAX_VALUE).forEach { heapBytes ->
            assertEquals(64 * mib, PlayerBufferPolicy.targetBufferBytes(heapBytes).toLong())
        }
    }
}
