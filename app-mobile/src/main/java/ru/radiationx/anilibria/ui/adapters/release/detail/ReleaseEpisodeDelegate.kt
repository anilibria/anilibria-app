package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseEpisodeBinding
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.extension.getCompatDrawable
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseEpisodeItemState
import ru.radiationx.shared.ktx.android.relativeDate

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseEpisodeDelegate(
    private val itemListener: Listener
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
        private val itemListener: Listener
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseEpisodeBinding>()

        fun bind(state: ReleaseEpisodeItemState, isEven: Boolean) {
            binding.itemTitle.text = state.title
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
            binding.itemViewedState.isVisible = state.isViewed
            binding.qualitySd.isVisible = state.hasSd
            binding.qualityHd.isVisible = state.hasHd
            binding.qualityFullHd.isVisible = state.hasFullHd

            binding.tvAction.isVisible = state.hasActionUrl
            if (state.hasActionUrl) {
                binding.tvAction.text = state.actionTitle
                val textColor = state.actionColorRes
                    ?.let { binding.tvAction.getCompatColor(it) }
                    ?: binding.tvAction.context.getColorFromAttr(R.attr.colorAccent)
                val iconDrawable =
                    state.actionIconRes?.let { binding.tvAction.getCompatDrawable(it) }

                binding.tvAction.setTextColor(textColor)
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    binding.tvAction,
                    null,
                    null,
                    iconDrawable,
                    null
                )
            }

            val bgColor = if (isEven) {
                binding.root.context.getColorFromAttr(R.attr.colorSurface)
            } else {
                binding.root.context.getColorFromAttr(R.attr.episode_even)
            }
            binding.root.setBackgroundColor(bgColor)

            binding.qualitySd.setOnClickListener {
                itemListener.onClickSd(state)
            }
            binding.qualityHd.setOnClickListener {
                itemListener.onClickHd(state)
            }
            binding.qualityFullHd.setOnClickListener {
                itemListener.onClickFullHd(state)
            }
            binding.root.setOnClickListener {
                itemListener.onClickEpisode(state)
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
        fun onClickSd(episode: ReleaseEpisodeItemState)

        fun onClickHd(episode: ReleaseEpisodeItemState)

        fun onClickFullHd(episode: ReleaseEpisodeItemState)

        fun onClickEpisode(episode: ReleaseEpisodeItemState)

        fun onLongClickEpisode(episode: ReleaseEpisodeItemState)
    }
}