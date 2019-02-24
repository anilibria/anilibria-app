package ru.radiationx.anilibria.ui.activities

import android.content.res.Configuration
import android.util.Log
import android.view.View

object PlayerWindowFlagHelper {

    fun getFlags(orientation: Int, fullScreen: Boolean): Int {
        Log.d("flaghelper", "getFlags $orientation, $fullScreen")
        return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            getPortraitFlags(fullScreen)
        } else {
            getLandscapeFlags()
        }
    }


    fun getPortraitFlags(fullScreen: Boolean): Int = if (fullScreen) {
        getPortraitFullscreenFlags()
    } else {
        getPortraitDefaultFlags()
    }

    fun getPortraitFullscreenFlags(): Int {

        Log.d("flaghelper", "getPortraitFullscreenFlags")
        var flags = View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        flags = flags or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        return flags
    }

    fun getPortraitDefaultFlags(): Int {
        Log.d("flaghelper", "getPortraitDefaultFlags")
        return View.SYSTEM_UI_FLAG_VISIBLE
    }

    fun getLandscapeFlags(): Int {
        Log.d("flaghelper", "getLandscapeFlags")
        var flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        flags = flags or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        return flags
    }

}