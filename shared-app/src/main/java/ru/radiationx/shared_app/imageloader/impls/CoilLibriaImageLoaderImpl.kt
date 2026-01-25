package ru.radiationx.shared_app.imageloader.impls

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import coil3.Image
import coil3.ImageLoader
import coil3.asDrawable
import coil3.load
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import ru.radiationx.data.di.providers.ApiClientWrapper
import ru.radiationx.shared_app.R
import ru.radiationx.shared_app.imageloader.ImageLoaderScopeConfig
import ru.radiationx.shared_app.imageloader.LibriaImageLoader
import ru.radiationx.shared_app.imageloader.utils.toCacheKey
import javax.inject.Inject

class CoilLibriaImageLoaderImpl @Inject constructor(
    private val context: Context,
    private val apiClientWrapper: ApiClientWrapper,
) : LibriaImageLoader {

    private var _okHttpClient: OkHttpClient? = null

    private var _imageLoader: ImageLoader? = null

    private val loaderMutex = Mutex()

    private fun getImageLoader(): ImageLoader {
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
            .components {
                add(OkHttpNetworkFetcherFactory(okHttpClient))
            }
            .build()
    }

    override fun showImage(imageView: ImageView, url: String?, config: ImageLoaderScopeConfig) {
        imageView.load(url, getImageLoader()) {
            val cacheKey = url.toCacheKey()
            diskCacheKey(cacheKey)
            memoryCacheKey(cacheKey)
            placeholderMemoryCacheKey(cacheKey)
            listener(
                onStart = {
                    config.onStart?.invoke()
                },
                onCancel = {
                    config.onCancel?.invoke()
                    config.onComplete?.invoke()
                },
                onError = { _: ImageRequest, errorResult: ErrorResult ->
                    config.onError?.invoke(errorResult.throwable)
                    config.onComplete?.invoke()
                },
                onSuccess = { _: ImageRequest, successResult: SuccessResult ->
                    imageView.successUrl = url
                    if (config.onSuccess != null) {
                        val bitmap = successResult.image.asBitmap(context)
                        config.onSuccess.invoke(bitmap)
                    }
                    config.onComplete?.invoke()
                }
            )
        }
    }

    override suspend fun loadImageBitmap(context: Context, url: String?): Bitmap? {
        val request = ImageRequest.Builder(context)
            .diskCacheKey(url.toCacheKey())
            .memoryCacheKey(url.toCacheKey())
            .data(url).build()
        val result = getImageLoader().execute(request)
        return result.image?.asBitmap(context)
    }

    private fun Image.asBitmap(context: Context): Bitmap? {
        return (asDrawable(context.resources) as? BitmapDrawable)?.bitmap
    }

    private var ImageView.successUrl: String?
        get() {
            return getTag(R.id.tag_image_loader_success_url) as String?
        }
        set(value) {
            setTag(R.id.tag_image_loader_success_url, value)
        }
}