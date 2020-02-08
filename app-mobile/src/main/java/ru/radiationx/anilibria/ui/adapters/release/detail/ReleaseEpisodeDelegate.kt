package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_episode.*
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.shared.ktx.asTimeSecString
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.shared.ktx.android.visible
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
            quality_full_hd.setOnClickListener {
                itemListener.onClickFullHd(currentItem)
            }
            containerView.setOnClickListener {
                itemListener.onClickEpisode(currentItem)
            }
        }

        fun bind(item: ReleaseFull.Episode, isEven: Boolean) {
            currentItem = item
            item_title.text = item.title

            Log.e("jojojo", "${item.id}=>${item.seek}")
            item_subtitle.text = "Остановлена на ${Date(item.seek).asTimeSecString()}"
            item_subtitle.visible(item.isViewed && item.seek > 0)

            item_viewed_state.visible(item.isViewed)

            quality_sd.visible(item.urlSd != null)
            quality_hd.visible(item.urlHd != null)
            quality_full_hd.visible(item.urlFullHd != null)

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

        fun onClickFullHd(episode: ReleaseFull.Episode)

        fun onClickEpisode(episode: ReleaseFull.Episode)
    }
}