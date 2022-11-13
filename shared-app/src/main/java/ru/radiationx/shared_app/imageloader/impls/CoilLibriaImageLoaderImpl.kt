package ru.radiationx.shared_app.imageloader.impls

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock
import android.widget.ImageView
import coil.ImageLoader
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import ru.radiationx.data.di.providers.ApiClientWrapper
import ru.radiationx.shared_app.imageloader.ImageLoaderScopeConfig
import ru.radiationx.shared_app.imageloader.LibriaImageLoader
import ru.radiationx.shared_app.imageloader.utils.toCacheKey
import toothpick.InjectConstructor

@InjectConstructor
class CoilLibriaImageLoaderImpl(
    private val context: Context,
    private val apiClientWrapper: ApiClientWrapper
) : LibriaImageLoader {

    private var _okHttpClient: OkHttpClient? = null

    private var _imageLoader: ImageLoader? = null

    private val loaderMutex = Mutex()

    private fun getImageLoader(): ImageLoader {
        val time = SystemClock.elapsedRealtime()
        val result = runBlocking {
            loaderMutex.withLock {
                val actualOkHttpClient = apiClientWrapper.get()
                val okHttpClient = _okHttpClient
                val imageLoader = _imageLoader
                if (imageLoader == null || okHttpClient != actualOkHttpClient) {
                    _imageLoader?.shutdown()
                    val newImageLoader = createImageLoader(actualOkHttpClient)
                    _okHttpClient = actualOkHttpClient
                    _imageLoader = newImageLoader
                    newImageLoader
                } else {
                    imageLoader
                }
            }
        }
        return result
    }

    private fun createImageLoader(okHttpClient: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .build()
    }

    override fun showImage(imageView: ImageView, url: String?, config: ImageLoaderScopeConfig) {
        imageView.load(url, getImageLoader()) {
            diskCacheKey(url.toCacheKey())
            memoryCacheKey(url.toCacheKey())
            listener(
                onStart = {
                    config.onStart?.invoke()
                },
                onCancel = {
                    config.onComplete?.invoke()
                },
                onError = { _: ImageRequest, errorResult: ErrorResult ->
                    config.onError?.invoke(errorResult.throwable)
                    config.onComplete?.invoke()
                },
                onSuccess = { _: ImageRequest, successResult: SuccessResult ->
                    val bitmap = (successResult.drawable as BitmapDrawable).bitmap
                    config.onSuccess?.invoke(bitmap)
                    config.onComplete?.invoke()
                }
            )
        }
    }

    override suspend fun loadImageBitmap(context: Context, url: String?): Bitmap {
        val request = ImageRequest.Builder(context)
            .diskCacheKey(url.toCacheKey())
            .memoryCacheKey(url.toCacheKey())
            .data(url).build()
        val result = getImageLoader().execute(request)
        val bitmap = (result.drawable as BitmapDrawable).bitmap
        return bitmap
    }
}