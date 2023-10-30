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
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchMenuItem
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.showWithLifecycle


class SearchCatalogFragment :
    BaseToolbarFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
    SharedProvider,
    ReleasesAdapter.ItemListener {

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
        listener = this,
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
        localClickListener = { searchViewModel.onLocalItemClick(it) }
    )

    private val searchViewModel by viewModel<FastSearchViewModel>()

    private val viewModel by viewModel<CatalogViewModel> {
        CatalogExtra(genre = getExtra(ARG_GENRE))
    }

    private var searchView: SearchMenuItem? = null

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

        searchView = SearchMenuItem(baseBinding.coordinatorLayout.context)
        genresDialog =
            CatalogFilterDialog(requireContext(), object : CatalogFilterDialog.ClickListener {
                override fun onAccept(state: CatalogFilterState) {
                    viewModel.onAcceptDialog(state)
                }

                override fun onClose() {
                    viewModel.onCloseDialog()
                }
            })

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
                    searchView?.requestFocus(it)
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
                    if (!hasFocus) {
                        searchViewModel.onClose()
                    } else {
                        viewModel.onFastSearchOpen()
                    }
                }

            })
            setOnQueryTextListener(object : SearchLayout.OnQueryTextListener {
                override fun onQueryTextSubmit(query: CharSequence): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: CharSequence): Boolean {
                    searchViewModel.onQueryChange(newText.toString())
                    return false
                }
            })

            setAdapter(fastSearchAdapter)
        }

        searchViewModel.state.onEach { state ->
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
            genresDialog.showDialog(state, viewLifecycleOwner)
        }.launchInResumed(viewLifecycleOwner)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        searchView?.layoutParams =
            (searchView?.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams?)?.apply {
                topMargin = dimensions.statusBar
            }
        searchView?.requestLayout()
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

}
