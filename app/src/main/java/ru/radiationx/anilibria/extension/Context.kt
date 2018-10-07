package ru.radiationx.anilibria.extension

import android.content.Context
import android.graphics.Color
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.util.TypedValue

@ColorInt
fun Context.getColorFromAttr(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    return if (this.theme.resolveAttribute(attr, typedValue, true))
        typedValue.data
    else
        Color.RED
}