package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseEpisodeBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseEpisodeItemState
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.data.api.releases.models.PlayerQuality
import ru.radiationx.shared.ktx.android.getColorFromAttr
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.android.getCompatDrawable
import ru.radiationx.shared.ktx.android.relativeDate
import ru.radiationx.shared_app.common.CompatDrawableSpan
import kotlin.math.roundToInt

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseEpisodeDelegate(
    private val itemListener: Listener,
) : AppAdapterDelegate<ReleaseEpisodeListItem, ListItem, ReleaseEpisodeDelegate.ViewHolder>(
    R.layout.item_release_episode,
    { it is ReleaseEpisodeListItem },
    { ViewHolder(it, itemListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 20

    override fun bindData(item: ReleaseEpisodeListItem, holder: ViewHolder) =
        holder.bind(item.state, item.isEven)

    class ViewHolder(
        itemView: View,
        private val itemListener: Listener,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseEpisodeBinding>()

        private val dimensionsApplier by dimensionsApplier()

        private val viewedDrawable by lazy {
            val size = (binding.root.context.resources.displayMetrics.density * 18).roundToInt()
            binding.root.context
                .getCompatDrawable(R.drawable.ic_checkbox_marked_circle)!!.mutate()
                .apply {
                    setBounds(0, 0, size, size)
                }
        }

        fun bind(state: ReleaseEpisodeItemState, isEven: Boolean) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemTitle.text = buildSpannedString {
                if (state.isViewed) {
                    inSpans(CompatDrawableSpan(viewedDrawable, CompatDrawableSpan.ALIGN_CENTER)) {
                        append(" ")
                    }
                    append(" ")
                }
                append(state.title)
            }
            binding.itemSubtitle.text = state.subtitle
            binding.itemSubtitle.isVisible = state.subtitle != null
            binding.itemDate.text = state.updatedAt
                ?.relativeDate(binding.itemDate.context)
                ?.let { "Обновлена $it" }
            binding.itemDate.isVisible = state.updatedAt != null
            val dateColor = if (state.hasUpdate) {
                binding.itemDate.context.getCompatColor(R.color.md_green)
            } else {
                binding.itemDate.context.getColorFromAttr(R.attr.textSecond)
            }
            binding.itemDate.setTextColor(dateColor)
            binding.qualitySd.isVisible = state.hasSd
            binding.qualityHd.isVisible = state.hasHd
            binding.qualityFullHd.isVisible = state.hasFullHd

            binding.tvAction.isVisible = state.hasActionUrl
            if (state.hasActionUrl) {
                binding.tvAction.text = state.actionTitle
                val textColor = state.actionColorRes
                    ?.let { binding.tvAction.getCompatColor(it) }
                    ?: binding.tvAction.context.getColorFromAttr(androidx.appcompat.R.attr.colorAccent)
                val iconDrawable =
                    state.actionIconRes?.let { binding.tvAction.getCompatDrawable(it) }

                binding.tvAction.setTextColor(textColor)
                binding.tvAction.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    iconDrawable,
                    null
                )
            }

            val bgColor = if (isEven) {
                binding.root.context.getColorFromAttr(com.google.android.material.R.attr.colorSurface)
            } else {
                binding.root.context.getColorFromAttr(R.attr.episode_even)
            }
            binding.root.setBackgroundColor(bgColor)

            binding.qualitySd.setOnClickListener {
                itemListener.onClickEpisode(state, PlayerQuality.SD)
            }
            binding.qualityHd.setOnClickListener {
                itemListener.onClickEpisode(state, PlayerQuality.HD)
            }
            binding.qualityFullHd.setOnClickListener {
                itemListener.onClickEpisode(state, PlayerQuality.FULLHD)
            }
            binding.root.setOnClickListener {
                itemListener.onClickEpisode(state, null)
            }
            binding.root.setOnLongClickListener {
                if (state.isViewed) {
                    itemListener.onLongClickEpisode(state)
                    true
                } else
                    false
            }
        }
    }

    interface Listener {

        fun onClickEpisode(episode: ReleaseEpisodeItemState, quality: PlayerQuality?)

        fun onLongClickEpisode(episode: ReleaseEpisodeItemState)
    }
}