package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_release_episode_control.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeControlItem
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseEpisodeControlDelegate(private val itemListener: Listener) : OptimizeDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is ReleaseEpisodeControlItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as ReleaseEpisodeControlItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_release_episode_control, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.run {
                full_button_watch_all.setOnClickListener {
                    itemListener.onClickWatchAll()
                }
                full_button_continue.setOnClickListener {
                    itemListener.onClickContinue()
                }
            }
        }

        fun bind(item: ReleaseFull) {
            view.run {
                val hasEpisodes = !item.episodes.isEmpty()
                val hasViewed = item.episodes.firstOrNull { it.isViewed } != null
                full_button_watch_all.isEnabled = hasEpisodes
                full_button_continue.visibility = if (hasViewed) View.VISIBLE else View.GONE
            }
        }
    }

    interface Listener {
        fun onClickWatchAll()

        fun onClickContinue()
    }
}