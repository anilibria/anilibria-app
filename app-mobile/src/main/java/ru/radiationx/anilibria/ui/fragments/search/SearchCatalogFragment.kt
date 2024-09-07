package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchMenuItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentTabsListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.data.apinext.models.Genre
import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.interactors.FilterType
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.showWithLifecycle


class SearchCatalogFragment :
    BaseToolbarFragment<FragmentTabsListRefreshBinding>(R.layout.fragment_tabs_list_refresh),
    SharedProvider,
    ReleasesAdapter.ItemListener,
    TopScroller {

    companion object {
        private const val ARG_TYPE = "arg_type"
        private const val ARG_GENRE = "arg_genre"

        fun newInstance(
            filterType: FilterType,
            genre: Genre? = null
        ) = SearchCatalogFragment().putExtra {
            putSerializable(ARG_TYPE, filterType)
            putParcelable(ARG_GENRE, genre)
        }
    }

    private lateinit var genresDialog: CatalogFilterDialog
    private val adapter = SearchAdapter(
        loadMoreListener = { viewModel.loadMore() },
        loadRetryListener = { viewModel.loadMore() },
        listener = this,
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

    private val viewModel by viewModel<FilterViewModel> {
        FilterExtra(
            type = getExtraNotNull(ARG_TYPE),
            genre = getExtra(ARG_GENRE)
        )
    }

    private var searchView: SearchMenuItem? = null

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

        searchView = SearchMenuItem(baseBinding.coordinatorLayout.context)
        genresDialog =
            CatalogFilterDialog(requireContext(), object : CatalogFilterDialog.ClickListener {
                override fun onAccept(state: CatalogFilterState) {
                    //viewModel.onAcceptDialog(state)
                }

                override fun onClose() {
                    //viewModel.onCloseDialog()
                }
            })

        binding.refreshLayout.setOnRefreshListener { viewModel.refresh() }

        binding.recyclerView.apply {
            adapter = this@SearchCatalogFragment.adapter
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
                    searchView?.requestFocus(it)
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            add("Фильтры")
                .setIcon(R.drawable.ic_filter_toolbar)
                .setOnMenuItemClickListener {
                    //viewModel.showDialog()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }


        baseBinding.coordinatorLayout.addView(searchView)
        searchView?.layoutParams =
            (searchView?.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams?)?.apply {
                width =
                    androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams.MATCH_PARENT
                height =
                    androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams.WRAP_CONTENT
                behavior = SearchBehavior<SearchMenuItem>()
            }


        (searchView as SearchLayout?)?.apply {
            setTextHint("Название релиза")
            setOnFocusChangeListener(object : SearchLayout.OnFocusChangeListener {
                override fun onFocusChange(hasFocus: Boolean) {
                    /* if (!hasFocus) {
                         searchViewModel.onClose()
                     } else {
                         viewModel.onFastSearchOpen()
                     }*/
                }

            })
            setOnQueryTextListener(object : SearchLayout.OnQueryTextListener {
                override fun onQueryTextSubmit(query: CharSequence): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: CharSequence): Boolean {
                    viewModel.onQueryChange(newText.toString())
                    return false
                }
            })
        }

        viewModel.state.onEach { state ->
            binding.progressBarList.isVisible = state.data.emptyLoading
            binding.refreshLayout.isRefreshing = state.data.refreshLoading
            adapter.bindState(state)
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
        /*viewModel.filterState.onEach { state ->
            val filtersCount = state.form.let {
                it.genres.size + it.years.size + it.seasons.size
            }
            var subtitle = ""
            subtitle += when (state.form.sort) {
                SearchForm.Sort.DATE -> "По новизне"
                SearchForm.Sort.RATING -> "По популярности"
            }
            subtitle += ", Фильтров: $filtersCount"
            baseBinding.toolbar.subtitle = subtitle
        }.launchIn(viewLifecycleOwner.lifecycleScope)*/

        /*viewModel.showFilterAction.observe().onEach { state ->
            genresDialog.showDialog(state, viewLifecycleOwner)
        }.launchInResumed(viewLifecycleOwner)*/
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        searchView?.layoutParams =
            (searchView?.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams?)?.apply {
                topMargin = dimensions.statusBar
            }
        searchView?.requestLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        searchView?.setAdapter(null)
        searchView = null
    }

    override fun onItemClick(position: Int, view: View) {
        sharedViewLocal = view
    }

    override fun onItemClick(item: ReleaseItemState, position: Int) {
        viewModel.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItemState): Boolean {
        val titles = arrayOf("Копировать ссылку", "Поделиться", "Добавить на главный экран")
        AlertDialog.Builder(requireContext())
            .setItems(titles) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.onCopyClick(item)
                        Toast.makeText(requireContext(), "Ссылка скопирована", Toast.LENGTH_SHORT)
                            .show()
                    }

                    1 -> viewModel.onShareClick(item)
                    2 -> viewModel.onShortcutClick(item)
                }
            }
            .showWithLifecycle(viewLifecycleOwner)
        return false
    }

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
        baseBinding.appbarLayout.setExpanded(true, true)
    }

}
