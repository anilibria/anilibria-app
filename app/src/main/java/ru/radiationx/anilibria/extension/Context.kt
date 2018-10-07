package ru.radiationx.anilibria.extension

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.StyleRes
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.util.TypedValue
import biz.source_code.miniTemplator.MiniTemplator
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder

@DrawableRes
fun Context.getDrawableResAttr(@AttrRes attr: Int): Int {
    val a = this.theme.obtainStyledAttributes(intArrayOf(attr))
    val attributeResourceId = a.getResourceId(0, 0)
    a.recycle()
    return attributeResourceId
}

fun Context.getDrawableAttr(@AttrRes attr: Int): Drawable {
    return ContextCompat.getDrawable(this, getDrawableResAttr(attr))!!
}

@ColorInt
fun Context.getColorFromAttr(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    return if (this.theme.resolveAttribute(attr, typedValue, true))
        typedValue.data
    else
        Color.RED
}

@StyleRes
fun AppThemeHolder.AppTheme.getMainStyleRes() = when (this) {
    AppThemeHolder.AppTheme.LIGHT -> R.style.LightAppTheme_NoActionBar
    AppThemeHolder.AppTheme.DARK -> R.style.DarkAppTheme_NoActionBar
}

@StyleRes
fun AppThemeHolder.AppTheme.getPrefStyleRes() = when (this) {
    AppThemeHolder.AppTheme.LIGHT -> R.style.PreferencesLightAppTheme
    AppThemeHolder.AppTheme.DARK -> R.style.PreferencesDarkAppTheme
}

fun AppThemeHolder.AppTheme.getWebStyleType() = when (this) {
    AppThemeHolder.AppTheme.LIGHT -> "light"
    AppThemeHolder.AppTheme.DARK -> "dark"
}

fun MiniTemplator.generateWithTheme(appTheme: AppThemeHolder.AppTheme): String {
    this.setVariableOpt("app_theme", appTheme.getWebStyleType())
    return generateOutput().also {
        reset()
    }
}