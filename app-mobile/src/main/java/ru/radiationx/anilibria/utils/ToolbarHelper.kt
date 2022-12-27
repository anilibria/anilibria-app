package ru.radiationx.anilibria.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.text.TextUtils
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.shared.ktx.android.asSoftware
import timber.log.Timber

/**
 * Created by radiationx on 23.12.17.
 */
object ToolbarHelper {

    fun setTransparent(toolbar: Toolbar, appBarLayout: AppBarLayout) {
        toolbar.setBackgroundColor(Color.TRANSPARENT)
        appBarLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    fun setScrollFlag(
        toolbarLayout: CollapsingToolbarLayout,
        @AppBarLayout.LayoutParams.ScrollFlags flag: Int
    ) {
        val params = toolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or flag
        toolbarLayout.layoutParams = params
    }

    fun fixInsets(target: Toolbar) {
        target.contentInsetStartWithNavigation = 0
        target.contentInsetEndWithActions = 0
        target.setContentInsetsAbsolute(0, 0)
    }

    fun marqueeTitle(target: Toolbar) {
        try {
            val toolbarTitleView: TextView
            val field = target::class.java.getDeclaredField("mTitleTextView")
            field.isAccessible = true
            toolbarTitleView = field.get(target) as TextView

            toolbarTitleView.ellipsize = TextUtils.TruncateAt.MARQUEE
            toolbarTitleView.setHorizontallyScrolling(true)
            toolbarTitleView.marqueeRepeatLimit = 3
            toolbarTitleView.isSelected = true
            toolbarTitleView.isHorizontalFadingEdgeEnabled = true
            toolbarTitleView.setFadingEdgeLength((target.resources.displayMetrics.density * 8).toInt())
        } catch (e: Exception) {
            Timber.w(e)
        }
    }

    suspend fun isDarkImage(bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.Default) {
            bitmap.asSoftware { bitmapCopy ->
                val histogram = IntArray(256) { 0 }
                for (x in 0 until bitmapCopy.width) {
                    for (y in 0 until bitmapCopy.height) {
                        val pixel = bitmapCopy.getPixel(x, y)
                        val r = Color.red(pixel)
                        val g = Color.green(pixel)
                        val b = Color.blue(pixel)

                        val brightness = (0.2126 * r + 0.7152 * g + 0.0722 * b).toInt()
                        histogram[brightness]++
                    }
                }

                val allPixelsCount = bitmapCopy.width * bitmapCopy.height
                val darkPixelCount = (0 until 64).sumBy { histogram[it] }

                darkPixelCount > allPixelsCount * 0.25
            }
        }
    }

}