package tv.aniliberty.next

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.allowHardware
import coil3.request.transformations

internal class PlayerEpisodeAdapter(
    private val items: List<Episode>,
    private val currentId: String?,
    private val progress: (String) -> Pair<Long, Long>,
    private val play: (Episode) -> Unit,
) : RecyclerView.Adapter<PlayerEpisodeAdapter.Holder>() {
    class Holder(
        val box: LinearLayout,
        val image: ImageView,
        val name: TextView,
        val status: TextView,
        val progress: ProgressBar,
    ) : RecyclerView.ViewHolder(box)

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val context = parent.context
        val box =
            LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
                isFocusable = true
                isClickable = true
                webControlBackground()
            }
        val image =
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                isFocusable = false
            }
        box.addView(
            image,
            LinearLayout.LayoutParams(box.dp(104), box.dp(60)).apply { marginEnd = box.dp(14) },
        )
        val info = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val name =
            TextView(context).apply {
                label("", 15f, WHITE, true)
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
        val status =
            TextView(context).apply {
                label("", 12f, MUTED)
                maxLines = 1
            }
        val bar =
            ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 1000
                progressTintList = android.content.res.ColorStateList.valueOf(RED)
                isFocusable = false
            }
        info.addView(name)
        info.addView(status, LinearLayout.LayoutParams(-1, -2).apply { topMargin = box.dp(5) })
        info.addView(bar, LinearLayout.LayoutParams(-1, box.dp(3)).apply { topMargin = box.dp(8) })
        box.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
        box.layoutParams =
            RecyclerView.LayoutParams(-1, box.dp(108)).apply { bottomMargin = box.dp(6) }
        return Holder(box, image, name, status, bar)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val episode = items[position]
        val (saved, duration) = progress(episode.id)
        holder.box.tag = "player-episode:${episode.id}"
        holder.name.text = episode.label
        holder.status.text =
            when {
                episode.id == currentId -> "▶ Сейчас воспроизводится"
                episode.streams.isEmpty() -> "Пока недоступно"
                isEpisodeWatched(saved, duration, TvSettings(holder.box.context).watchedPercent) -> "✓ Просмотрено"
                saved > 0 -> "Продолжить с ${playbackTime(saved)}"
                else -> "${number(episode.ordinal)} эпизод"
            }
        holder.progress.progress =
            if (duration > 0) (saved.coerceIn(0, duration) * 1000 / duration).toInt() else 0
        holder.progress.visibility = if (saved > 0) View.VISIBLE else View.GONE
        holder.image.load(episode.preview) {
            if (saved <= 0 && episode.id != currentId) {
                allowHardware(false)
                size(208, 120)
                transformations(SpoilerBlurTransformation())
            }
        }
        holder.box.isEnabled = episode.streams.isNotEmpty()
        holder.box.isFocusable = episode.streams.isNotEmpty()
        holder.box.alpha = if (episode.streams.isEmpty()) .45f else 1f
        holder.box.setOnClickListener { if (episode.streams.isNotEmpty()) play(episode) }
    }
}
