package ru.radiationx.shared_app.common

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.download.ImageDownloader
import ru.radiationx.shared_app.common.ImageFileNameGenerator

object ImageLoaderConfig {

    fun init(context: Context, imageDownloader: ImageDownloader) {
        val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(Handler())
            .displayer(FadeInBitmapDisplayer(500, true, true, false))


        val config = ImageLoaderConfiguration.Builder(context)
            .threadPoolSize(5)
            .threadPriority(Thread.MIN_PRIORITY)
            .denyCacheImageMultipleSizesInMemory()
            .imageDownloader(imageDownloader)
            .memoryCache(UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
            .diskCacheSize(25 * 1024 * 1024)
            .diskCacheFileNameGenerator(ImageFileNameGenerator())
            .defaultDisplayImageOptions(defaultOptionsUIL.build())
            .build()
        ImageLoader.getInstance().init(config)
    }
}