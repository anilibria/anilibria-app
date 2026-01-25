package ru.radiationx.anilibria.utils.dimensions

import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView.ViewHolder

fun View.getPaddingOffsets(): Offsets {
    return Offsets(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

fun View.getMarginOffsets(): Offsets {
    return Offsets(marginLeft, marginTop, marginRight, marginBottom)
}

fun View.dimensionsApplier(): Lazy<DimensionsApplier> = lazy {
    DimensionsApplier(this)
}

fun ViewHolder.dimensionsApplier(): Lazy<DimensionsApplier> = lazy {
    DimensionsApplier(itemView)
}