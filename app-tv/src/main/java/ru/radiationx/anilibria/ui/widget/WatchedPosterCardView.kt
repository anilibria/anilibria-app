package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.leanback.widget.ImageCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.observePosterWatched
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.quill.Quill

open class WatchedPosterCardView(context: Context) : ImageCardView(context) {
    private var releaseId: ReleaseId? = null
    private var series: String? = null
    private var watchScope: CoroutineScope? = null
    private var allEpisodesViewed = false
    private val stripeHeight = 3f * resources.displayMetrics.density
    private val stripePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.alib_red)
    }
    private val releaseInteractor by lazy { Quill.getRootScope().get(ReleaseInteractor::class) }

    fun bindWatchedRelease(id: ReleaseId?, series: String? = null) {
        stopWatching()
        releaseId = id
        this.series = series
        updateWatched(false)
        if (isAttachedToWindow) startWatching()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startWatching()
    }

    override fun onDetachedFromWindow() {
        stopWatching()
        super.onDetachedFromWindow()
    }

    private fun startWatching() {
        stopWatching()
        val id = releaseId ?: return
        val scope = CoroutineScope(Dispatchers.Main.immediate + Job())
        watchScope = scope
        scope.launch {
            releaseInteractor.observePosterWatched(id, series).collect { updateWatched(it) }
        }
    }

    private fun stopWatching() {
        watchScope?.cancel()
        watchScope = null
    }

    private fun updateWatched(value: Boolean) {
        allEpisodesViewed = value
        ViewCompat.setStateDescription(this, "Все серии просмотрены".takeIf { value })
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!allEpisodesViewed) return
        val image = mainImageView ?: return
        canvas.drawRect(
            image.left.toFloat(), image.bottom - stripeHeight,
            image.right.toFloat(), image.bottom.toFloat(), stripePaint,
        )
    }
}
