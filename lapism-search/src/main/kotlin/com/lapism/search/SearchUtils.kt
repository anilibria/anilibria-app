package com.lapism.search

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

enum class NavigationIcon {
    Arrow,
    Search
}

enum class MarginsType {
    NoneToolbar,
    NoneMenuItem,
    Toolbar,
    MenuItem
}

object SearchUtils {

    @JvmStatic
    fun fadeAddFocus(view: View?, duration: Long) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.duration = duration
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                view?.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {

            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        view?.startAnimation(anim)
    }

    @JvmStatic
    fun fadeRemoveFocus(view: View?, duration: Long) {
        val anim = AlphaAnimation(1.0f, 0.0f)
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.duration = duration
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                view?.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        view?.startAnimation(anim)
    }

}
