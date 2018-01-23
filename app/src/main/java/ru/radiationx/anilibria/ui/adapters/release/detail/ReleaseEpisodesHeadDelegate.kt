package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodesHeadListItem

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseEpisodesHeadDelegate : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is ReleaseEpisodesHeadListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {}

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_release_head_episodes, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}