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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.releaseItemDialog
import ru.radiationx.anilibria.ui.fragments.BaseSearchItemFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.utils.dimensions.Dimensions
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.putExtra


class SearchCatalogFragment :
    BaseSearchItemFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
    SharedProvider,
    TopScroller {

    companion object {
        private const val ARG_GENRE = "arg_genre"

        fun newInstance(genre: String? = null) = SearchCatalogFragment().putExtra {
            putString(ARG_GENRE, genre)
        }
    }

    private lateinit var genresDialog: CatalogFilterDialog
    private val adapter = SearchAdapter(
        loadMoreListener = { viewModel.loadMore() },
        loadRetryListener = { viewModel.loadMore() },
        clickListener = { item, view ->
            this.sharedViewLocal = view
            viewModel.onItemClick(item)
        },
        longClickListener = { item -> releaseDialog.show(item) },
        remindCloseListener = { viewModel.onRemindClose() },
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

    private val fastSearchAdapter = FastSearchAdapter(
        clickListener = { searchViewModel.onItemClick(it) },
        localClickListener = { searchViewModel.onLocalItemClick(it) },
        retryClickListener = { searchViewModel.refresh() }
    )

    private val searchViewModel by viewModel<FastSearchViewModel>()

    private val viewModel by viewModel<CatalogViewModel> {
        CatalogExtra(genre = getExtra(ARG_GENRE))
    }

    private val releaseDialog by releaseItemDialog(
        onCopyClick = { viewModel.onCopyClick(it) },
        onShareClick = { viewModel.onShareClick(it) },
        onShortcutClick = { viewModel.onShortcutClick(it) }
    )

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override val statusBarVisible: Boolean = true

    override fun onCreateBinding(view: View): FragmentListRefreshBinding {
        return FragmentListRefreshBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postopneEnterTransitionWithTimout()
        binding.recyclerView.doOnLayout {
            startPostponedEnterTransition()
        }


        genresDialog = CatalogFilterDialog(
            requireContext(),
            viewLifecycleOwner,
            object : CatalogFilterDialog.ClickListener {
                override fun onAccept(state: CatalogFilterState) {
                    viewModel.onAcceptDialog(state)
                }

                override fun onClose() {
                    viewModel.onCloseDialog()
                }
            }
        )

        binding.refreshLayout.setOnRefreshListener { viewModel.refreshReleases() }

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
                    viewModel.onFastSearchClick()
                    baseBinding.searchView.show()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            add("Фильтры")
                .setIcon(R.drawable.ic_filter_toolbar)
                .setOnMenuItemClickListener {
                    viewModel.showDialog()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }


        baseBinding.searchView.apply {
            setHint("Название релиза")
            setOnFocusChangeListener { hasFocus ->
                if (hasFocus) {
                    viewModel.onFastSearchOpen()
                }
            }
            setOnQueryTextListener { newText ->
                searchViewModel.onQueryChange(newText)
            }

            setContentAdapter(fastSearchAdapter)
        }

        searchViewModel.state.onEach { state ->
            baseBinding.searchView.setLoading(state.loaderState.loading)
            fastSearchAdapter.bindItems(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.onEach { state ->
            binding.progressBarList.isVisible = state.data.emptyLoading
            binding.refreshLayout.isRefreshing = state.data.refreshLoading
            adapter.bindState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.filterState.onEach { state ->
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
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.showFilterAction.observe().onEach { state ->
            genresDialog.showDialog(state)
        }.launchInResumed(viewLifecycleOwner)
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
