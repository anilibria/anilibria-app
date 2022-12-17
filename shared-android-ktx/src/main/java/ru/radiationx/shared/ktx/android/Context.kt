package ru.radiationx.shared.ktx.android

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.*
import androidx.core.content.ContextCompat
import android.util.TypedValue
import android.view.View

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


fun Context.getCompatDrawable(@DrawableRes icRes: Int): Drawable? = ContextCompat.getDrawable(this, icRes)
fun View.getCompatDrawable(@DrawableRes icRes: Int): Drawable? = context.getCompatDrawable(icRes)
fun Context.getCompatColor(@ColorRes icRes: Int): Int = ContextCompat.getColor(this, icRes)
fun View.getCompatColor(@ColorRes icRes: Int): Int = context.getCompatColor(icRes)

fun Context.dpToPx(dp: Int): Int = (this.resources.displayMetrics.density * dp).toInt()
fun View.dpToPx(dp: Int): Int = this.context.dpToPx(dp)
