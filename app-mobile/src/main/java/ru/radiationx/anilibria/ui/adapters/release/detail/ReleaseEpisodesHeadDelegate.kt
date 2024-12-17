package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseHeadEpisodesBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodesHeadListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.EpisodesTabState
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared.ktx.android.getColorFromAttr
import ru.radiationx.shared.ktx.android.getCompatColor

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
        itemView: View,
        private val itemListener: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseHeadEpisodesBinding>()

        private val dimensionsApplier by dimensionsApplier()

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
            binding.tabLayout.addOnTabSelectedListener(tabListener)
        }

        fun bind(item: ReleaseEpisodesHeadListItem) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            currentItem = item
            binding.tabLayout.removeOnTabSelectedListener(tabListener)
            updateSelectedColors(item.tabs, item.selectedTag)
            updateTabsItems(item.tabs)
            updateSelectedTab(item.selectedTag)
            binding.tabLayout.addOnTabSelectedListener(tabListener)
        }

        private fun updateSelectedColors(tabs: List<EpisodesTabState>, selectedTag: String?) {
            val selectedState = tabs.find { it.tag == selectedTag }
            val selectedColor = selectedState?.textColor
                ?.let { binding.tabLayout.getCompatColor(it) }
                ?: binding.tabLayout.context.getColorFromAttr(androidx.appcompat.R.attr.colorAccent)
            binding.tabLayout.setTabTextColors(
                binding.tabLayout.context.getColorFromAttr(R.attr.textSecond),
                selectedColor
            )
            binding.tabLayout.setSelectedTabIndicatorColor(selectedColor)
        }

        private fun updateSelectedTab(selectedTag: String?) {
            (0 until binding.tabLayout.tabCount)
                .mapNotNull { binding.tabLayout.getTabAt(it) }
                .firstOrNull { it.tag == selectedTag }
                .also { binding.tabLayout.selectTab(it) }
        }

        private fun updateTabsItems(tabStates: List<EpisodesTabState>) {
            if (tabStates.size != binding.tabLayout.tabCount) {
                binding.tabLayout.removeAllTabs()
                repeat(tabStates.size) {
                    binding.tabLayout.addTab(binding.tabLayout.newTab())
                }
            }
            tabStates.forEachIndexed { index, tabState ->
                val tab = binding.tabLayout.getTabAt(index)
                tab?.tag = tabState.tag
                tab?.text = tabState.title
            }
        }
    }
}