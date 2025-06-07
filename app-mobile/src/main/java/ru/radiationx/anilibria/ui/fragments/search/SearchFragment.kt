package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentTabsListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.releaseItemDialog
import ru.radiationx.anilibria.ui.fragments.BaseSearchItemFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.search.filter.SearchFilterDialog
import ru.radiationx.anilibria.ui.fragments.search.filter.SearchFilterViewModel
import ru.radiationx.anilibria.utils.dimensions.Dimensions
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.releases.models.ReleaseGenre
import ru.radiationx.data.api.shared.filter.FilterType
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.putExtra
import taiwa.lifecycle.lifecycleLazy


class SearchFragment :
    BaseSearchItemFragment<FragmentTabsListRefreshBinding>(R.layout.fragment_tabs_list_refresh),
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

    private val adapter = SearchAdapter(
        loadMoreListener = { viewModel.loadMore() },
        loadRetryListener = { viewModel.loadMore() },
        clickListener = { item, view ->
            this.sharedViewLocal = view
            viewModel.onItemClick(item)
        },
        longClickListener = { item -> releaseDialog.show(item) },
        emptyPlaceHolder = PlaceholderListItem(
            R.drawable.ic_toolbar_search,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_search
        ),
        errorPlaceHolder = PlaceholderListItem(
            R.drawable.ic_toolbar_search,
            R.string.placeholder_title_errordata_base,
            R.string.placeholder_desc_nodata_base
        )
    )

    private val viewModel by viewModel<SearchViewModel> {
        SearchExtra(
            type = getExtraNotNull(ARG_TYPE),
            genre = getExtra(ARG_GENRE)
        )
    }

    private val filterViewModel by viewModel<SearchFilterViewModel>()

    private val releaseDialog by releaseItemDialog(
        onCopyClick = { viewModel.onCopyClick(it) },
        onShareClick = { viewModel.onShareClick(it) },
        onShortcutClick = { viewModel.onShortcutClick(it) }
    )

    private val filterDialog by lifecycleLazy {
        SearchFilterDialog(
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
            viewModel = filterViewModel
        )
    }

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override val statusBarVisible: Boolean = true

    override fun onCreateBinding(view: View): FragmentTabsListRefreshBinding {
        return FragmentTabsListRefreshBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postopneEnterTransitionWithTimout()
        binding.recyclerView.doOnLayout {
            startPostponedEnterTransition()
        }


        binding.refreshLayout.setOnRefreshListener { viewModel.refresh() }

        binding.recyclerView.apply {
            adapter = this@SearchFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            disableItemChangeAnimation()
        }

        ToolbarShadowController(
            binding.recyclerView,
            baseBinding.appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        //ToolbarHelper.fixInsets(toolbar)
        with(baseBinding.toolbar) {
            title = "Поиск"
            /*setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)*/
        }

        baseBinding.toolbar.menu.apply {
            add("Поиск")
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener {
                    //viewModel.onFastSearchClick()
                    baseBinding.searchView.show()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            add("Фильтры")
                .setIcon(R.drawable.ic_filter_toolbar)
                .setOnMenuItemClickListener {
                    filterDialog.show()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }


        baseBinding.searchView.apply {
            setHint("Название релиза")
            setOnQueryTextListener { newText ->
                viewModel.onQueryChange(newText)
            }
        }

        viewModel.state.onEach { state ->
            binding.progressBarList.isVisible = state.releases.emptyLoading
            binding.refreshLayout.isRefreshing = state.releases.refreshLoading
            adapter.bindState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        combine(
            viewModel.state.map { it.filter }.distinctUntilChanged(),
            filterViewModel.state
        ) { filterState, form ->
            filterDialog.setForm(filterState, form)
        }.launchIn(viewLifecycleOwner.lifecycleScope)


        val tabListener = object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.selectCollection(tab.tag as CollectionType)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }

        }
        binding.tabLayout.addOnTabSelectedListener(tabListener)


        viewModel.collections
            .onEach { binding.tabLayout.isVisible = it != null }
            .filterNotNull()
            .onEach { state ->
                binding.tabLayout.removeOnTabSelectedListener(tabListener)
                binding.tabLayout.removeAllTabs()
                state.types.forEach { type ->
                    val tab = binding.tabLayout.newTab().apply {
                        setTag(type)
                        setText(type.toString())
                    }
                    binding.tabLayout.addTab(tab, type == state.selected)
                }
                binding.tabLayout.addOnTabSelectedListener(tabListener)
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
        binding.recyclerView.adapter = null
        baseBinding.searchView.setContentAdapter(null)
    }

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
        baseBinding.appbarLayout.setExpanded(true, true)
    }

}
