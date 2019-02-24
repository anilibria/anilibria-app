package ru.radiationx.anilibria.ui.adapters.release.detail

import android.content.res.ColorStateList
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_episode.*
import kotlinx.android.synthetic.main.item_release_episode.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.setTint
import ru.radiationx.anilibria.extension.setTintColorAttr
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

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
            holder.bind(item.item, item.isEven)

    class ViewHolder(
            override val containerView: View,
            private val itemListener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: ReleaseFull.Episode
        private val disableColor = R.attr.base_icon
        private val enableColor = R.attr.colorAccent

        init {
            quality_sd.setOnClickListener {
                itemListener.onClickSd(currentItem)
            }
            quality_hd.setOnClickListener {
                itemListener.onClickHd(currentItem)
            }
            containerView.setOnClickListener {
                itemListener.onClickEpisode(currentItem)
            }
        }

        fun bind(item: ReleaseFull.Episode, isEven: Boolean) {
            currentItem = item
            item_title.text = item.title
            item_viewed_state.visibility = if (item.isViewed) View.VISIBLE else View.GONE

            quality_sd.isEnabled = item.urlSd != null
            quality_hd.isEnabled = item.urlHd != null
            quality_sd.setTintColorAttr(if (item.urlSd != null) enableColor else disableColor)
            quality_hd.setTintColorAttr(if (item.urlHd != null) enableColor else disableColor)

            val bgColor = if (isEven) {
                containerView.context.getColorFromAttr(R.attr.cardBackground)
            } else {
                containerView.context.getColorFromAttr(R.attr.episode_even)
            }
            containerView.setBackgroundColor(bgColor)
        }
    }

    interface Listener {
        fun onClickSd(episode: ReleaseFull.Episode)

        fun onClickHd(episode: ReleaseFull.Episode)

        fun onClickEpisode(episode: ReleaseFull.Episode)
    }
}