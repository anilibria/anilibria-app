package ru.radiationx.shared_app.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import ru.radiationx.data.common.Url

interface LibriaImageLoader {

    fun showImage(imageView: ImageView, url: Url?, config: ImageLoaderScopeConfig)

    suspend fun loadImageBitmap(context: Context, url: Url?): Bitmap?
}