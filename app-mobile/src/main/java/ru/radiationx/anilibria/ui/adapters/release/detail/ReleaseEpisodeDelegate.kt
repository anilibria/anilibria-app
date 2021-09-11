package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_episode.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.presentation.release.details.ReleaseEpisodeItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import java.util.*

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
        override val containerView: View,
        private val itemListener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(state: ReleaseEpisodeItemState, isEven: Boolean) {
            item_title.text = state.title
            item_subtitle.text = state.subtitle
            item_subtitle.isVisible = state.subtitle != null
            item_viewed_state.isVisible = state.isViewed
            quality_sd.isVisible = state.hasSd
            quality_hd.isVisible = state.hasHd
            quality_full_hd.isVisible = state.hasFullHd

            val bgColor = if (isEven) {
                containerView.context.getColorFromAttr(R.attr.colorSurface)
            } else {
                containerView.context.getColorFromAttr(R.attr.episode_even)
            }
            containerView.setBackgroundColor(bgColor)

            quality_sd.setOnClickListener {
                itemListener.onClickSd(state)
            }
            quality_hd.setOnClickListener {
                itemListener.onClickHd(state)
            }
            quality_full_hd.setOnClickListener {
                itemListener.onClickFullHd(state)
            }
            containerView.setOnClickListener {
                itemListener.onClickEpisode(state)
            }
            containerView.setOnLongClickListener {
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