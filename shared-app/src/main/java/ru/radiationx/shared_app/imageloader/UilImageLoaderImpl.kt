package ru.radiationx.shared_app.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.view.View
import android.widget.ImageView
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.download.ImageDownloader
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.shared_app.common.ImageFileNameGenerator

class UilImageLoaderImpl(
    context: Context,
    imageDownloader: ImageDownloader
) : LibriaImageLoader {

    init {
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

    override fun showImage(imageView: ImageView, url: String?, config: ImageLoaderScopeConfig) {
        ImageLoader.getInstance().displayImage(url, imageView, object : ImageLoadingListener {
            override fun onLoadingStarted(imageUri: String?, view: View?) {
                config.onStart?.invoke()
            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                config.onError?.invoke(
                    failReason?.cause ?: RuntimeException(failReason?.type?.name.orEmpty())
                )
                config.onComplete?.invoke()
            }

            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                loadedImage?.also { config.onSuccess?.invoke(it) }
                config.onComplete?.invoke()
            }

            override fun onLoadingCancelled(imageUri: String?, view: View?) {
                config.onComplete?.invoke()
            }
        })
    }

    override suspend fun loadImageBitmap(context: Context, url: String?): Bitmap {
        return withContext(Dispatchers.IO) {
            ImageLoader.getInstance().loadImageSync(url)
        }
    }
}