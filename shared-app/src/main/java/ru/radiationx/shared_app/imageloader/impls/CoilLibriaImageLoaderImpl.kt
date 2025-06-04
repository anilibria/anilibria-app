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
import okhttp3.OkHttpClient
import ru.radiationx.data.app.config.AppConfig
import ru.radiationx.data.common.Url
import ru.radiationx.data.common.withBase
import ru.radiationx.data.di.DirectClient
import ru.radiationx.shared_app.R
import ru.radiationx.shared_app.imageloader.ImageLoaderScopeConfig
import ru.radiationx.shared_app.imageloader.LibriaImageLoader
import ru.radiationx.shared_app.imageloader.utils.toCacheKey
import javax.inject.Inject

class CoilLibriaImageLoaderImpl @Inject constructor(
    private val context: Context,
    @DirectClient private val okHttpClient: OkHttpClient,
    private val appConfig: AppConfig
) : LibriaImageLoader {

    private val imageLoader by lazy {
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(okHttpClient))
            }
            .build()
    }

    override fun showImage(imageView: ImageView, url: Url?, config: ImageLoaderScopeConfig) {
        val cacheKey = url.toCacheKey()
        val absoluteUrl = url?.withBase(appConfig.image)
        imageView.load(absoluteUrl, imageLoader) {
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
                    imageView.successUrl = absoluteUrl
                    if (config.onSuccess != null) {
                        val bitmap = successResult.image.asBitmap(context)
                        config.onSuccess.invoke(bitmap)
                    }
                    config.onComplete?.invoke()
                }
            )
        }
    }

    override suspend fun loadImageBitmap(context: Context, url: Url?): Bitmap? {
        val cacheKey = url.toCacheKey()
        val absoluteUrl = url?.withBase(appConfig.image)
        val request = ImageRequest.Builder(context)
            .diskCacheKey(cacheKey)
            .memoryCacheKey(cacheKey)
            .data(absoluteUrl)
            .build()
        val result = imageLoader.execute(request)
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