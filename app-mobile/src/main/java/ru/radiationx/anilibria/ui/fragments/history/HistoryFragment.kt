package ru.radiationx.anilibria.ui.fragments.history

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
import ru.radiationx.anilibria.extension.setAndAwaitItems
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.common.release.showContextRelease
import ru.radiationx.anilibria.ui.fragments.BaseSearchItemFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.feed.FeedToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.anilibria.utils.dimensions.Dimensions
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.controllers.loaderpage.hasAnyLoading

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryFragment :
    BaseSearchItemFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
    SharedProvider,
    TopScroller {

    companion object {
        private const val ARG_IMPORT_URI = "import_uri"

        fun newInstance(importUri: Uri?) = HistoryFragment().putExtra {
            putParcelable(ARG_IMPORT_URI, importUri)
        }
    }

    private var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    private val adapter = ReleasesAdapter(
        loadMoreListener = { viewModel.loadMore() },
        loadRetryListener = { viewModel.loadMore() },
        importListener = {
            importLauncher.launch("application/json")
        },
        exportListener = {
            fileViewModel.onExportClick()
        },
        clickListener = { item, view ->
            this.sharedViewLocal = view
            viewModel.onItemClick(item)
        },
        longClickListener = { item ->
            viewModel.onItemContextClick(item)
        },
        emptyPlaceHolder = PlaceholderListItem(
            R.drawable.ic_history,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_history
        ),
        errorPlaceHolder = PlaceholderListItem(
            R.drawable.ic_history,
            R.string.placeholder_title_errordata_base,
            R.string.placeholder_desc_nodata_base
        )
    )

    private val searchAdapter = ListItemAdapter().apply {
        addDelegate(
            ReleaseItemDelegate(
                clickListener = { item, view ->
                    sharedViewLocal = view
                    viewModel.onItemClick(item)
                },
                longClickListener = { item ->
                    viewModel.onItemContextClick(item)
                }
            )
        )
    }

    private val viewModel by viewModel<HistoryViewModel>()
    private val fileViewModel by viewModel<HistoryFileViewModel>()

    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            fileViewModel.onImportFileSelected(it)
        }
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

        ToolbarHelper.fixInsets(baseBinding.toolbar)

        baseBinding.toolbar.apply {
            title = "История"
            setNavigationOnClickListener { viewModel.onBackPressed() }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        baseBinding.toolbar.menu.apply {
            add("Поиск")
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener {
                    viewModel.onSearchClick()
                    baseBinding.searchView.show()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        FeedToolbarShadowController(
            binding.recyclerView,
            baseBinding.appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@HistoryFragment.adapter
            disableItemChangeAnimation()
        }

        binding.refreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        baseBinding.searchView.apply {
            setHint("Название релиза")
            setOnQueryTextListener { newText ->
                viewModel.localSearch(newText)
            }

            setContentAdapter(searchAdapter)
        }

        viewModel.state.onEach {
            showState(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.contextEvent.onEach {
            showContextRelease(it.id, it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        val importUri = getExtra<Uri>(ARG_IMPORT_URI)
        if (importUri != null) {
            fileViewModel.onImportFileSelected(importUri)
            arguments?.remove(ARG_IMPORT_URI)
        }
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

    private suspend fun showState(state: HistoryScreenState) {
        binding.progressBarList.isVisible = state.data.emptyLoading
        binding.refreshLayout.isRefreshing = state.data.refreshLoading
        baseBinding.searchView.setLoading(state.data.hasAnyLoading())
        adapter.bindState(state.data, withExport = true)
        searchAdapter.setAndAwaitItems(state.searchItems.map { ReleaseListItem(it) })
    }
}