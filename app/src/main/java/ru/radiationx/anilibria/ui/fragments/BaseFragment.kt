package ru.radiationx.anilibria.ui.fragments;

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatFragment
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.common.BackButtonListener

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment : MvpAppCompatFragment(), BackButtonListener {
    abstract val layoutRes: Int

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater.inflate(R.layout.fragment_main_base, container, false)
        inflater.inflate(layoutRes, newView?.findViewById(R.id.fragment_content), true)
        return newView
    }

    protected fun fitSystemWindow() {
        //coordinator_layout.fitsSystemWindows = true
        //appbarLayout.fitsSystemWindows = true
        //toolbarLayout.fitsSystemWindows = true
        //toolbar.fitsSystemWindows = true
    }

    protected fun fixToolbarInsets(target: Toolbar) {
        target.contentInsetStartWithNavigation = 0
        target.contentInsetEndWithActions = 0
        target.setContentInsetsAbsolute(0, 0)
    }

    protected fun setMarqueeTitle(target: Toolbar) {
        try {
            val toolbarTitleView: TextView
            val field = target.javaClass.getDeclaredField("mTitleTextView")
            field.isAccessible = true
            toolbarTitleView = field.get(target) as TextView

            toolbarTitleView.ellipsize = TextUtils.TruncateAt.MARQUEE
            toolbarTitleView.setHorizontallyScrolling(true)
            toolbarTitleView.marqueeRepeatLimit = 3
            toolbarTitleView.isSelected = true
            toolbarTitleView.isHorizontalFadingEdgeEnabled = true
            toolbarTitleView.setFadingEdgeLength((resources.displayMetrics.density * 8).toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isDarkImage(bitmap: Bitmap): Boolean {
        val histogram = IntArray(256, { i: Int -> 0 })

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                //val color = bitmap.getRGB(x, y)

                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val brightness = (0.2126 * r + 0.7152 * g + 0.0722 * b).toInt()
                histogram[brightness]++;
            }
        }

        val allPixelsCount = bitmap.width * bitmap.height;
        val darkPixelCount = (0 until 64).sumBy { histogram[it] }
        return darkPixelCount > allPixelsCount * 0.25
    }
}
