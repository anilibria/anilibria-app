package ru.radiationx.data.player

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
class PlayerCacheDataSourceProvider @Inject constructor(
    private val context: Context
) {

    private val cache by lazy {
        val directory = File(context.cacheDir, "player")
        SimpleCache(
            directory,
            LeastRecentlyUsedCacheEvictor(1024 * 1024 * 50),
            StandaloneDatabaseProvider(context)
        )
    }

    fun createCacheFactory(upstreamFactory: DataSource.Factory): DataSource.Factory {
        val cacheSink = CacheDataSink.Factory().setCache(cache)
        val downStreamFactory = FileDataSource.Factory()
        return CacheDataSource.Factory()
            .setCache(cache)
            .setCacheWriteDataSinkFactory(cacheSink)
            .setCacheReadDataSourceFactory(downStreamFactory)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}