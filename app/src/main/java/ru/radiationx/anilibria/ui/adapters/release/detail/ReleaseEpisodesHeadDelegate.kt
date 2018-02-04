package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.design.widget.TabLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_release_head_episodes.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodesHeadListItem

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseEpisodesHeadDelegate(private val itemListener: Listener) : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is ReleaseEpisodesHeadListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        (holder as ViewHolder).bind()
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_release_head_episodes, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val tabListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    itemListener.onSelect(it.tag as String, layoutPosition)
                }
            }
        }

        init {
            view.run {
                tabLayout.addTab(tabLayout.newTab().setText("Онлайн").setTag("online"))
                tabLayout.addTab(tabLayout.newTab().setText("Скачать").setTag("download"))
                tabLayout.addOnTabSelectedListener(tabListener)
            }
        }

        fun bind() {
            view.run {

            }
        }
    }

    interface Listener {
        fun onSelect(tabTag: String, position: Int)
    }
}