package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentTabsViewPagerBinding
import ru.radiationx.anilibria.databinding.ViewTabCustomBinding
import ru.radiationx.anilibria.ui.fragments.BaseSearchFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.search.di.SearchModule
import ru.radiationx.anilibria.ui.fragments.search.filter.SearchFilterDialog
import ru.radiationx.anilibria.ui.fragments.search.filter.SearchFilterExtra
import ru.radiationx.anilibria.ui.fragments.search.filter.SearchFilterViewModel
import ru.radiationx.anilibria.ui.fragments.search.tab.SearchTabFragment
import ru.radiationx.anilibria.utils.dimensions.Dimensions
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.releases.models.ReleaseGenre
import ru.radiationx.data.api.shared.filter.FilterType
import ru.radiationx.quill.installModules
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import searchbar.NavigationIcon
import taiwa.lifecycle.lifecycleLazy


class SearchFragment :
    BaseSearchFragment<FragmentTabsViewPagerBinding>(R.layout.fragment_tabs_view_pager),
    SharedProvider,
    TopScroller {

    companion object {
        private const val ARG_TYPE = "arg_type"
        private const val ARG_GENRE = "arg_genre"

        fun newInstance(
            filterType: FilterType,
            genre: ReleaseGenre? = null
        ) = SearchFragment().putExtra {
            putSerializable(ARG_TYPE, filterType)
            putParcelable(ARG_GENRE, genre)
        }
    }

    private val argType by lazy {
        getExtraNotNull<FilterType>(ARG_TYPE)
    }

    private val viewModel by viewModel<SearchViewModel> {
        SearchExtra(type = argType)
    }

    private val filterViewModel by viewModel<SearchFilterViewModel> {
        SearchFilterExtra(
            type = argType,
            genre = getExtra(ARG_GENRE)
        )
    }

    private val filterDialog by lifecycleLazy {
        SearchFilterDialog(
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
            viewModel = filterViewModel
        )
    }

    private val pagerListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            getPagerAdapter().getTab(position).collectionType?.also {
                viewModel.onCollectionChanged(it)
            }
        }
    }

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override val statusBarVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installModules(SearchModule())
        super.onCreate(savedInstanceState)
    }

    override fun onCreateBinding(view: View): FragmentTabsViewPagerBinding {
        return FragmentTabsViewPagerBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.apply {
            offscreenPageLimit = CollectionType.knownTypes.size
            adapter = TabsAdapter(this@SearchFragment)
            registerOnPageChangeCallback(pagerListener)
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val adapterTab = getPagerAdapter().getTab(position)
            val title = when (adapterTab.collectionType) {
                CollectionType.Planned -> "Запланировано"
                CollectionType.Watching -> "Смотрю"
                CollectionType.Watched -> "Просмотрено"
                CollectionType.Postponed -> "Отложено"
                CollectionType.Abandoned -> "Брошено"
                is CollectionType.Unknown -> adapterTab.collectionType.raw
                null -> "null"
            }
            tab.setCustomView(R.layout.view_tab_custom)
            tab.setText(title)
            tab.setBadgeText(adapterTab.count?.toString())
        }.attach()

        baseBinding.searchView.apply {
            val hint = when (argType) {
                FilterType.Collections -> "Поиск по коллекции"
                FilterType.Favorites -> "Поиск по избранному"
                FilterType.Catalog -> "Поиск по каталогу"
            }
            setHint(hint)
            setNavigationIcon(NavigationIcon.Search)
            setExpandable(false)
            setOnQueryTextListener { newText ->
                viewModel.onQueryChange(newText)
            }
            setOnMenuClickListener {
                filterDialog.show()
            }
        }

        filterViewModel.state.onEach {
            if (it.form.hasChanges()) {
                baseBinding.searchView.setMenuIcon(R.drawable.ic_filter_changes_toolbar)
            } else {
                baseBinding.searchView.setMenuIcon(R.drawable.ic_filter_toolbar)
            }
            filterDialog.setForm(it.filter, it.form)
        }.launchIn(viewLifecycleOwner.lifecycleScope)


        viewModel.collections
            .onEach { state ->
                binding.tabLayout.isVisible = state != null
            }
            .map { state ->
                state?.types
                    ?.map { TabsAdapter.Tab(argType, it, state.counts[it]) }
                    ?: listOf(TabsAdapter.Tab(argType, null, null))
            }
            .distinctUntilChanged()
            .onEach { tabs ->
                binding.viewPager.unregisterOnPageChangeCallback(pagerListener)
                getPagerAdapter().updateTabs(tabs)
                binding.viewPager.registerOnPageChangeCallback(pagerListener)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        baseBinding.searchView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            topMargin = dimensions.top
        }
        baseBinding.searchView.setFieldInsets(Insets.of(dimensions.left, 0, dimensions.right, 0))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
        binding.viewPager.unregisterOnPageChangeCallback(pagerListener)
    }

    override fun scrollToTop() {
        baseBinding.appbarLayout.setExpanded(true, true)
        childFragmentManager
            .fragments
            .filter { it.isResumed && it.isVisible && it.isAdded }
            .filterIsInstance<TopScroller>()
            .firstOrNull()
            ?.scrollToTop()
    }

    private fun getPagerAdapter(): TabsAdapter {
        return binding.viewPager.adapter as TabsAdapter
    }

    private class TabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        private var currentTabs = listOf<Tab>()

        fun updateTabs(tabs: List<Tab>) {
            if (tabs == currentTabs) {
                return
            }
            currentTabs = tabs.toList()
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return currentTabs.size
        }

        override fun createFragment(position: Int): Fragment {
            val tab = getTab(position)
            return SearchTabFragment.newInstance(tab.filterType, tab.collectionType)
        }

        fun getTab(position: Int): Tab {
            return currentTabs[position]
        }

        data class Tab(
            val filterType: FilterType,
            val collectionType: CollectionType?,
            val count: Int?
        )
    }

    private fun TabLayout.Tab.setBadgeText(text: String?) {
        val tabBinding = ViewTabCustomBinding.bind(requireNotNull(customView))
        tabBinding.tabBadge.text = text
        tabBinding.tabBadge.isVisible = text != null
    }
}
