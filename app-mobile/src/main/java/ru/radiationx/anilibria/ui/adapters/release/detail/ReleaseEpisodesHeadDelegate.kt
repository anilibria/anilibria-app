package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_head_episodes.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodesHeadListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.entity.app.release.ReleaseFull

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseEpisodesHeadDelegate(
    private val itemListener: (ReleaseFull.Episode.Type) -> Unit
) : AppAdapterDelegate<ReleaseEpisodesHeadListItem, ListItem, ReleaseEpisodesHeadDelegate.ViewHolder>(
    R.layout.item_release_head_episodes,
    { it is ReleaseEpisodesHeadListItem },
    { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseEpisodesHeadListItem, holder: ViewHolder) =
        holder.bind(item.episodeType)

    class ViewHolder(
        override val containerView: View,
        private val itemListener: (ReleaseFull.Episode.Type) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val tabListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                itemListener.invoke(tab.tag as ReleaseFull.Episode.Type)
            }
        }

        init {
            tabLayout.addTab(
                tabLayout.newTab().setText("Онлайн").setTag(ReleaseFull.Episode.Type.ONLINE)
            )
            tabLayout.addTab(
                tabLayout.newTab().setText("Скачать").setTag(ReleaseFull.Episode.Type.SOURCE)
            )
            tabLayout.addOnTabSelectedListener(tabListener)
        }

        fun bind(episodeType: ReleaseFull.Episode.Type) {
            tabLayout.removeOnTabSelectedListener(tabListener)
            (0 until tabLayout.tabCount)
                .mapNotNull { tabLayout.getTabAt(it) }
                .firstOrNull { it.tag == episodeType }
                ?.also { tabLayout.selectTab(it) }
            tabLayout.addOnTabSelectedListener(tabListener)
        }
    }
}