package ru.radiationx.anilibria.extension

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.DisplayMetrics
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.radiationx.anilibria.R
import ru.radiationx.shared.ktx.android.getColorFromAttr

fun BottomSheetDialog.fillNavigationBarColor() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return
    val window = window ?: return

    val metrics = DisplayMetrics()
    window.windowManager.defaultDisplay.getMetrics(metrics)

    val dimDrawable = GradientDrawable()

    val navigationBarDrawable = GradientDrawable()
    navigationBarDrawable.shape = GradientDrawable.RECTANGLE
    navigationBarDrawable.setColor(context.getColorFromAttr(R.attr.colorSurface))

    val layers = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)

    val windowBackground = LayerDrawable(layers)
    windowBackground.setLayerInsetTop(1, metrics.heightPixels)

    window.setBackgroundDrawable(windowBackground)
}