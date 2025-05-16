package ru.radiationx.shared_app.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView

interface LibriaImageLoader {

    fun showImage(imageView: ImageView, url: String?, config: ImageLoaderScopeConfig)

    suspend fun loadImageBitmap(context: Context, url: String?): Bitmap?
}