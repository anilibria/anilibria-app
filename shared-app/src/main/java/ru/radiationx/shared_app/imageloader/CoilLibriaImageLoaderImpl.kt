package ru.radiationx.shared_app.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import coil.imageLoader
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult

class CoilLibriaImageLoaderImpl(
    private val context: Context
) : LibriaImageLoader {
    override fun showImage(imageView: ImageView, url: String?, config: ImageLoaderScopeConfig) {
        imageView.load(url) {
            listener(
                onStart = {
                    config.onStart?.invoke()
                },
                onCancel = {
                    config.onComplete?.invoke()
                },
                onError = { imageRequest: ImageRequest, errorResult: ErrorResult ->
                    config.onError?.invoke(errorResult.throwable)
                    config.onComplete?.invoke()
                },
                onSuccess = { imageRequest: ImageRequest, successResult: SuccessResult ->
                    val bitmap = (successResult.drawable as BitmapDrawable).bitmap
                    config.onSuccess?.invoke(bitmap)
                    config.onComplete?.invoke()
                }
            )
        }
    }

    override suspend fun loadImageBitmap(url: String?): Bitmap {
        val request = ImageRequest.Builder(context).data(url).build()
        val result = context.imageLoader.execute(request)
        val bitmap = (result.drawable as BitmapDrawable).bitmap
        return bitmap
    }
}