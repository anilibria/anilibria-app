package ru.radiationx.anilibria.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.App

/**
 * Created by radiationx on 23.12.17.
 */
object ToolbarHelper {

    fun setTransparent(toolbar: Toolbar, appBarLayout: AppBarLayout) {
        toolbar.setBackgroundColor(Color.TRANSPARENT)
        appBarLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    fun setScrollFlag(toolbarLayout: CollapsingToolbarLayout, @AppBarLayout.LayoutParams.ScrollFlags flag: Int) {
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
            Log.e("S_DEF_LOG", "" + field + " : " + target + " : " + field.get(target))
            toolbarTitleView = field.get(target) as TextView

            toolbarTitleView.ellipsize = TextUtils.TruncateAt.MARQUEE
            toolbarTitleView.setHorizontallyScrolling(true)
            toolbarTitleView.marqueeRepeatLimit = 3
            toolbarTitleView.isSelected = true
            toolbarTitleView.isHorizontalFadingEdgeEnabled = true
            toolbarTitleView.setFadingEdgeLength((App.instance.resources.displayMetrics.density * 8).toInt())
        } catch (e: Exception) {
            Log.e("error", e.message)
        }
    }

    fun isDarkImage(bitmap: Bitmap, onSuccess: Consumer<Boolean>) {
        Single.defer {
            val histogram = IntArray(256, { i: Int -> 0 })

            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)

                    val brightness = (0.2126 * r + 0.7152 * g + 0.0722 * b).toInt()
                    histogram[brightness]++
                }
            }

            val allPixelsCount = bitmap.width * bitmap.height
            val darkPixelCount = (0 until 64).sumBy { histogram[it] }
            Single.just(darkPixelCount > allPixelsCount * 0.25)
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess)
    }
}