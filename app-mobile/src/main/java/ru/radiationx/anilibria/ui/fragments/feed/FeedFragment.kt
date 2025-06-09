package ru.radiationx.anilibria.ui.fragments.feed

import android.os.Bundle
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
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.release.showContextRelease
import ru.radiationx.anilibria.ui.common.youtubeItemDialog
import ru.radiationx.anilibria.ui.fragments.BaseSearchFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.search.fast.FastSearchAdapter
import ru.radiationx.anilibria.ui.fragments.search.fast.FastSearchViewModel
import ru.radiationx.anilibria.utils.dimensions.Dimensions
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import searchbar.NavigationIcon


/* Created by radiationx on 05.11.17. */

class FeedFragment :
    BaseSearchFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
    SharedProvider,
    TopScroller {

    private val adapter = FeedAdapter(
        loadMoreListener = {
            viewModel.loadMore()
        }, loadRetryListener = {
            viewModel.loadMore()
        }, warningClickListener = {
            viewModel.appWarningClick(it)
        }, warningClickCloseListener = {
            viewModel.appWarningCloseClick(it)
        }, donationListener = {
            viewModel.onDonationClick(it)
        }, donationCloseListener = {
            viewModel.onDonationCloseClick(it)
        }, schedulesClickListener = {
            viewModel.onSchedulesClick()
        }, scheduleScrollListener = { position ->
            viewModel.onScheduleScroll(position)
        }, randomClickListener = {
            viewModel.onRandomClick()
        }, releaseClickListener = { releaseItem, view ->
            this.sharedViewLocal = view
            viewModel.onItemClick(releaseItem)
        }, releaseLongClickListener = { releaseItem ->
            viewModel.onItemContextClick(releaseItem)
        }, youtubeClickListener = { youtubeItem ->
            viewModel.onYoutubeClick(youtubeItem)
        },
        youtubeLongClickListener = {
            youtubeDialog.show(it)
        }, scheduleClickListener = { feedScheduleItem, view, position ->
            this.sharedViewLocal = view
            viewModel.onScheduleItemClick(feedScheduleItem, position)
        }, emptyPlaceHolder = PlaceholderListItem(
            R.drawable.ic_newspaper,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_search
        ), errorPlaceHolder = PlaceholderListItem(
            R.drawable.ic_newspaper,
            R.string.placeholder_title_errordata_base,
            R.string.placeholder_desc_nodata_base
        )
    )

    private val searchAdapter = FastSearchAdapter(
        clickListener = { searchViewModel.onItemClick(it) },
        localClickListener = { searchViewModel.onLocalItemClick(it) },
        retryClickListener = { searchViewModel.refresh() }
    )

    private val viewModel by viewModel<FeedViewModel>()

    private val searchViewModel by viewModel<FastSearchViewModel>()

    private val youtubeDialog by youtubeItemDialog(
        onCopyClick = { viewModel.onCopyClick(it) },
        onShareClick = { viewModel.onShareClick(it) },
    )

    private var sharedViewLocal: View? = null

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

        binding.refreshLayout.setOnRefreshListener { viewModel.refreshReleases() }
        binding.recyclerView.apply {
            adapter = this@FeedFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            disableItemChangeAnimation()
        }

        baseBinding.toolbar.apply {
            title = getString(R.string.fragment_title_releases)
            title = "Лента"
        }

        FeedToolbarShadowController(
            binding.recyclerView,
            baseBinding.appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        baseBinding.searchView.apply {
            setHint("Поиск по названию")
            setNavigationIcon(NavigationIcon.Search)
            setOnFocusChangeListener { hasFocus ->
                if (hasFocus) {
                    setNavigationIcon(NavigationIcon.Arrow)
                    viewModel.onFastSearchOpen()
                    baseBinding.appbarLayout.setExpanded(true)
                } else {
                    setNavigationIcon(NavigationIcon.Search)
                }
            }
            setOnQueryTextListener { newText ->
                searchViewModel.onQueryChange(newText)
            }

            setContentAdapter(searchAdapter)
        }

        viewModel.state.onEach { state ->
            binding.progressBarList.isVisible = state.data.emptyLoading
            binding.refreshLayout.isRefreshing = state.data.refreshLoading
            adapter.bindState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.contextEvent.onEach {
            showContextRelease(it.id, it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        searchViewModel.state.onEach { state ->
            baseBinding.searchView.setLoading(state.loaderState.loading)
            searchAdapter.bindItems(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        baseBinding.searchView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            topMargin = dimensions.top
        }
        baseBinding.searchView.setFieldInsets(Insets.of(dimensions.left, 0, dimensions.right, 0))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        adapter.restoreState(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.saveState(null)
        binding.recyclerView.adapter = null
        baseBinding.searchView.setContentAdapter(null)
    }

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
        baseBinding.appbarLayout.setExpanded(true, true)
    }
}
