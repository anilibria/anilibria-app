package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import ru.radiationx.anilibria.R

class PosterCardView(context: Context) : ImageCardView(context) {

    var showFavoriteBadge: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val density = resources.displayMetrics.density
    private val badgeBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        alpha = 128
    }
    private val star = requireNotNull(ContextCompat.getDrawable(context, R.drawable.ic_poster_favorite))

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!showFavoriteBadge) return
        val image = mainImageView ?: return
        val radius = 16f * density
        val centerX = image.right - 8f * density - radius
        val centerY = image.top + 8f * density + radius
        canvas.drawCircle(centerX, centerY, radius, badgeBackground)
        val halfStar = 12f * density
        star.setBounds(
            (centerX - halfStar).toInt(),
            (centerY - halfStar).toInt(),
            (centerX + halfStar).toInt(),
            (centerY + halfStar).toInt(),
        )
        star.draw(canvas)
    }
}
