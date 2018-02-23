package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_release_episode.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeListItem

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseEpisodeDelegate(private val itemListener: Listener) : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is ReleaseEpisodeListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as ReleaseEpisodeListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_release_episode, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.run {
                quality_sd.setOnClickListener({
                    itemListener.onClickSd(view.tag as ReleaseFull.Episode)
                })
                quality_hd.setOnClickListener({
                    itemListener.onClickHd(view.tag as ReleaseFull.Episode)
                })
                view.setOnClickListener {
                    itemListener.onClickEpisode(view.tag as ReleaseFull.Episode)
                }
            }
        }

        fun bind(item: ReleaseFull.Episode) {
            view.run {
                view.tag = item
                item_title.text = item.title
                item_viewed_state.visibility = if (item.isViewed) View.VISIBLE else View.GONE
            }
        }
    }

    interface Listener {
        fun onClickSd(episode: ReleaseFull.Episode)

        fun onClickHd(episode: ReleaseFull.Episode)

        fun onClickEpisode(episode: ReleaseFull.Episode)
    }
}