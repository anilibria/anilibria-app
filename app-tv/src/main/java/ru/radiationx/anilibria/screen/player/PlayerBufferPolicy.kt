package ru.radiationx.anilibria.screen.player

internal object PlayerBufferPolicy {
    private const val MIN_BUFFER_BYTES = 64 * 1024L
    private const val MAX_BUFFER_BYTES = 64 * 1024 * 1024L

    fun targetBufferBytes(maxHeapBytes: Long): Int {
        // Reserve most of the heap for the app, HTTP buffers and the current HLS chunk,
        // which can take the allocator beyond its target. Keep large heaps bounded too.
        return (maxHeapBytes / 8).coerceIn(MIN_BUFFER_BYTES, MAX_BUFFER_BYTES).toInt()
    }
}
