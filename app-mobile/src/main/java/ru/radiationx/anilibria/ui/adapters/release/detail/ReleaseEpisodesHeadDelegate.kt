package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_head_episodes.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.presentation.release.details.EpisodesTabState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodesHeadListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseEpisodesHeadDelegate(
    private val itemListener: (String) -> Unit
) : AppAdapterDelegate<ReleaseEpisodesHeadListItem, ListItem, ReleaseEpisodesHeadDelegate.ViewHolder>(
    R.layout.item_release_head_episodes,
    { it is ReleaseEpisodesHeadListItem },
    { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseEpisodesHeadListItem, holder: ViewHolder) =
        holder.bind(item)

    class ViewHolder(
        override val containerView: View,
        private val itemListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private var currentItem: ReleaseEpisodesHeadListItem? = null

        private val tabListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                val selectedTag = tab.tag as String
                currentItem?.also { updateSelectedColors(it.tabs, selectedTag) }
                itemListener.invoke(selectedTag)
            }
        }

        init {
            tabLayout.addOnTabSelectedListener(tabListener)
        }

        fun bind(item: ReleaseEpisodesHeadListItem) {
            currentItem = item
            tabLayout.removeOnTabSelectedListener(tabListener)
            updateSelectedColors(item.tabs, item.selectedTag)
            updateTabsItems(item.tabs)
            updateSelectedTab(item.selectedTag)
            tabLayout.addOnTabSelectedListener(tabListener)
        }

        private fun updateSelectedColors(tabs: List<EpisodesTabState>, selectedTag: String?) {
            val selectedState = tabs.find { it.tag == selectedTag }
            val selectedColor = selectedState?.textColor
                ?.let { tabLayout.getCompatColor(it) }
                ?: tabLayout.context.getColorFromAttr(R.attr.colorAccent)
            tabLayout.setTabTextColors(
                tabLayout.context.getColorFromAttr(R.attr.textSecond),
                selectedColor
            )
            tabLayout.setSelectedTabIndicatorColor(selectedColor)
        }

        private fun updateSelectedTab(selectedTag: String?) {
            (0 until tabLayout.tabCount)
                .mapNotNull { tabLayout.getTabAt(it) }
                .firstOrNull { it.tag == selectedTag }
                .also { tabLayout.selectTab(it) }
        }

        private fun updateTabsItems(tabStates: List<EpisodesTabState>) {
            if (tabStates.size != tabLayout.tabCount) {
                tabLayout.removeAllTabs()
                repeat(tabStates.size) {
                    tabLayout.addTab(tabLayout.newTab())
                }
            }
            tabStates.forEachIndexed { index, tabState ->
                val tab = tabLayout.getTabAt(index)
                tab?.tag = tabState.tag
                tab?.text = tabState.title
            }
        }
    }
}