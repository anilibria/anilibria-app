package ru.radiationx.anilibria.screen.player

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.leanback.widget.SeekBar
import ru.radiationx.anilibria.R
import kotlin.math.max

/** Draws a stable track while Leanback's seek bar continues to handle focus, keys and accessibility. */
@SuppressLint("RestrictedApi")
internal class PlayerTimelineDrawable(private val seekBar: SeekBar, progressColor: Int) : Drawable() {
    private val host = seekBar.parent as FrameLayout
    private val resources = seekBar.resources
    private val density = resources.displayMetrics.density
    private val barHeight = resources.getDimensionPixelSize(
        androidx.leanback.R.dimen.lb_playback_transport_progressbar_bar_height,
    )
    private val activeRadius = resources.getDimensionPixelSize(
        androidx.leanback.R.dimen.lb_playback_transport_progressbar_active_radius,
    )
    private val originallyWillNotDraw = seekBar.willNotDraw()
    private val originalDescription = seekBar.contentDescription
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerColor = ContextCompat.getColor(seekBar.context, R.color.player_timeline_marker)
    private val playedMarkerColor = ContextCompat.getColor(
        seekBar.context, R.color.player_timeline_marker_played,
    )
    private val playedColor = if (progressColor != Color.TRANSPARENT) progressColor else {
        seekBar.context.obtainStyledAttributes(intArrayOf(
            androidx.leanback.R.attr.playbackProgressPrimaryColor,
        )).let { attributes ->
            try {
                attributes.getColor(0, ContextCompat.getColor(
                    seekBar.context, androidx.leanback.R.color.lb_playback_progress_color_no_theme,
                ))
            } finally {
                attributes.recycle()
            }
        }
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f * resources.displayMetrics.scaledDensity
        setShadowLayer(2f * density, 0f, density, Color.BLACK)
    }
    private var segments = emptyList<PlayerTimelineSegment>()
    private var duration = 0L
    private var drawnState: TrackState? = null
    private val layoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        setBounds(0, 0, host.width, host.height)
    }
    private val drawListener = ViewTreeObserver.OnPreDrawListener {
        // Native seek/preview/buffer updates invalidate the hidden SeekBar. Mirror them here.
        if (drawnState != trackState()) invalidateSelf()
        true
    }

    init {
        setBounds(0, 0, host.width, host.height)
        host.addOnLayoutChangeListener(layoutListener)
        host.viewTreeObserver.addOnPreDrawListener(drawListener)
        // The native track changes its horizontal coordinates with focus. Hide only its drawing;
        // an overlay on its parent uses the same coordinates for all visual parts of the timeline.
        seekBar.setWillNotDraw(true)
        host.overlay.add(this)
    }

    fun update(segments: List<PlayerTimelineSegment>, duration: Long) {
        this.segments = segments
        this.duration = duration
        seekBar.contentDescription = if (segments.isEmpty()) originalDescription else {
            segments.joinToString(". ") {
                resources.getString(
                    R.string.player_timeline_range,
                    label(it), formatTime(it.start), formatTime(it.end),
                )
            }
        }
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        drawnState = trackState()
        if (host.width <= activeRadius * 2) return
        val radius = if (seekBar.isFocused) activeRadius else barHeight / 2
        val centerY = (host.height / 2).toFloat()
        val trackStart = activeRadius.toFloat()
        val trackWidth = (host.width - activeRadius * 2).toFloat()
        val top = centerY - barHeight / 2f
        val bottom = centerY + barHeight / 2f
        fun progressX(value: Int) = trackStart + if (seekBar.max > 0) {
            value.toFloat() / seekBar.max * trackWidth
        } else 0f
        val playedEnd = progressX(seekBar.progress)
        val bufferedEnd = progressX(seekBar.secondProgress)
        paint.color = Color.GRAY
        canvas.drawRoundRect(trackStart, top, trackStart + trackWidth, bottom,
            barHeight / 2f, barHeight / 2f, paint)
        if (bufferedEnd > playedEnd) {
            paint.color = seekBar.secondaryProgressColor
            canvas.drawRect(playedEnd, top, bufferedEnd, bottom, paint)
        }
        paint.color = playedColor
        canvas.drawRect(trackStart, top, playedEnd, bottom, paint)
        fun x(position: Long) = trackStart + (position.toDouble() / duration * trackWidth).toFloat()
        if (duration > 0) {
            segments.forEach { segment ->
                val start = x(segment.start)
                val end = x(segment.end)
                paint.color = markerColor
                canvas.drawRect(start, top, end, bottom, paint)
                if (playedEnd > start) {
                    paint.color = playedMarkerColor
                    canvas.drawRect(start, top, minOf(end, playedEnd), bottom, paint)
                }
            }
            drawLabels(canvas, ::x, centerY - activeRadius - 5f * density)
        }
        paint.color = Color.WHITE
        canvas.drawCircle(playedEnd, centerY, radius.toFloat(), paint)
    }

    private fun drawLabels(canvas: Canvas, x: (Long) -> Float, baseline: Float) {
        val margin = 2f * density
        val labels = segments.map { segment ->
            val text = label(segment)
            val width = textPaint.measureText(text)
            val center = (x(segment.start) + x(segment.end)) / 2f
            Label(segment, text, width, (center - width / 2f)
                .coerceIn(margin, max(margin, host.width - margin - width)))
        }
        // Adjacent or overlapping ranges still need distinct readable labels.
        if (labels.size == 2 && labels[0].left + labels[0].width + 8f * density > labels[1].left) {
            labels[0].left = margin
            labels[1].left = max(margin, host.width - margin - labels[1].width)
        }
        labels.forEach {
            textPaint.color = markerColor
            canvas.drawText(it.text, it.left, baseline, textPaint)
        }
    }

    private fun label(segment: PlayerTimelineSegment): String = resources.getString(
        when (segment.type) {
            PlayerTimelineSegment.Type.OPENING -> R.string.player_timeline_opening
            PlayerTimelineSegment.Type.ENDING -> R.string.player_timeline_ending
        },
    )

    private fun formatTime(position: Long): String {
        val seconds = position / 1000
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }

    fun dispose() {
        host.removeOnLayoutChangeListener(layoutListener)
        if (host.viewTreeObserver.isAlive) host.viewTreeObserver.removeOnPreDrawListener(drawListener)
        host.overlay.remove(this)
        seekBar.setWillNotDraw(originallyWillNotDraw)
        seekBar.contentDescription = originalDescription
    }

    private fun trackState() = TrackState(seekBar.progress, seekBar.secondProgress,
        seekBar.max, seekBar.isFocused, seekBar.secondaryProgressColor)

    private data class TrackState(
        val progress: Int,
        val bufferedProgress: Int,
        val max: Int,
        val focused: Boolean,
        val bufferedColor: Int,
    )

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        textPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        textPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private data class Label(
        val segment: PlayerTimelineSegment,
        val text: String,
        val width: Float,
        var left: Float,
    )
}
