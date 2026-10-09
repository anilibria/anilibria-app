package ru.radiationx.anilibria.screen.player

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import androidx.leanback.widget.PlaybackTransportRowPresenter
import androidx.leanback.widget.RowPresenter
import androidx.leanback.widget.SeekBar
import androidx.media3.common.util.UnstableApi
import ru.radiationx.data.entity.domain.release.PlayerSkips

@SuppressLint("RestrictedApi")
@UnstableApi
internal class PlayerTimelineRowPresenter(
    private val glue: VideoPlayerGlue,
) : PlaybackTransportRowPresenter() {

    private val timelines = mutableMapOf<RowPresenter.ViewHolder, PlayerTimelineDrawable>()
    private var skips: PlayerSkips? = null
    private var duration = 0L

    init {
        setDescriptionPresenter(object : AbstractDetailsDescriptionPresenter() {
            override fun onBindDescription(viewHolder: ViewHolder, item: Any) {
                viewHolder.title.text = glue.title
                viewHolder.subtitle.text = glue.subtitle
            }
        })
    }

    fun setSkips(skips: PlayerSkips?) {
        this.skips = skips
        // A new media item must not use the previous episode's duration.
        duration = 0L
        updateTimelines()
    }

    fun setDuration(duration: Long) {
        if (this.duration == duration) return
        this.duration = duration
        updateTimelines()
    }

    private fun updateTimelines() {
        val segments = skips.timelineSegments(duration)
        timelines.values.forEach { it.update(segments, duration) }
    }

    override fun createRowViewHolder(parent: ViewGroup): RowPresenter.ViewHolder {
        val holder = super.createRowViewHolder(parent)
        val seekBar = holder.view.findViewById<SeekBar>(androidx.leanback.R.id.playback_progress)
        val seekParent = seekBar.parent as ViewGroup
        val index = seekParent.indexOfChild(seekBar)
        val params = seekBar.layoutParams.apply {
            // Reserve label space before the first layout, including while media is loading.
            height = (48f * parent.resources.displayMetrics.density).toInt()
        }
        seekParent.removeView(seekBar)
        val timeline = FrameLayout(parent.context).apply {
            isFocusable = false
            addView(seekBar, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
            ))
        }
        seekParent.addView(timeline, index, params)
        return holder
    }

    override fun onBindRowViewHolder(holder: RowPresenter.ViewHolder, item: Any) {
        super.onBindRowViewHolder(holder, item)
        holder.onKeyListener = glue
        timelines.remove(holder)?.dispose()
        val seekBar = holder.view.findViewById<SeekBar>(androidx.leanback.R.id.playback_progress)
        timelines[holder] = PlayerTimelineDrawable(seekBar, progressColor).apply {
            update(skips.timelineSegments(duration), duration)
        }
    }

    override fun onUnbindRowViewHolder(holder: RowPresenter.ViewHolder) {
        timelines.remove(holder)?.dispose()
        holder.onKeyListener = null
        super.onUnbindRowViewHolder(holder)
    }
}
