package ru.radiationx.anilibria.screen.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec

@UnstableApi
internal class HlsBufferingDataSource(
    private val upstream: DataSource,
    private val gate: BufferReadGate,
) : DataSource by upstream {
    private var isTransportStream = false

    override fun open(dataSpec: DataSpec): Long {
        // The TV's HLS MPEG-TS segments expose samples while extraction is in progress.
        // Do not throttle playlists, keys or other container formats during preparation.
        isTransportStream = dataSpec.uri.path?.endsWith(".ts", ignoreCase = true) == true
        return upstream.open(dataSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (!isTransportStream || length == 0) return upstream.read(buffer, offset, length)
        gate.awaitCapacity()
        // This bounds each additional source read, not HTTP or extractor-owned memory.
        return upstream.read(buffer, offset, minOf(length, 64 * 1024))
    }
}
