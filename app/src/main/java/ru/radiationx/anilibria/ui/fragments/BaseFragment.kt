package ru.radiationx.anilibria.ui.fragments;

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatFragment
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
}
