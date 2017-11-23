package ru.radiationx.anilibria.ui.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_base.*

import java.lang.reflect.Field;

import ru.radiationx.anilibria.R;

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment : MvpAppCompatFragment() {

    abstract val layoutRes: Int

    @Deprecated("Pls no override")
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater?.inflate(R.layout.fragment_main_base, container, false)
        inflater?.inflate(layoutRes, newView?.findViewById(R.id.fragment_content), true)
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

    protected fun baseInflateFragment(inflater: LayoutInflater, @LayoutRes res: Int) {
        inflater.inflate(res, fragment_content, true)
    }

}
