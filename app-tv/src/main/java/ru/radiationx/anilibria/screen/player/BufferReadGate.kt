package ru.radiationx.anilibria.screen.player

import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.atomic.AtomicInteger

internal class BufferReadGate(
    private val limitBytes: Int,
    private val allocatedBytes: () -> Int,
    private val isPlaybackPaused: () -> Boolean,
    private val stallTimeoutMs: Long = 30_000,
) {
    val waitCount = AtomicInteger()
    val peakAllocatedBytes = AtomicInteger()

    fun awaitCapacity() {
        var waiting = false
        var lastAllocatedBytes = allocatedBytes()
        var progressAt = System.nanoTime()
        while (true) {
            val allocated = allocatedBytes()
            peakAllocatedBytes.accumulateAndGet(allocated, ::maxOf)
            if (allocated < limitBytes) return
            if (!waiting) {
                waitCount.incrementAndGet()
                waiting = true
            }
            val now = System.nanoTime()
            if (allocated < lastAllocatedBytes || isPlaybackPaused()) {
                progressAt = now
            } else if ((now - progressAt) / 1_000_000 >= stallTimeoutMs) {
                // Non-interleaved or malformed streams may be unable to free samples.
                // Fail the load rather than grow the buffer or wait indefinitely.
                throw BufferCapacityException()
            }
            lastAllocatedBytes = allocated
            try {
                Thread.sleep(20)
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                throw InterruptedIOException("Video load cancelled while waiting for buffer space")
                    .apply { initCause(interrupted) }
            }
        }
    }
}

internal class BufferCapacityException : IOException("Video buffer cannot make progress within its memory budget")
