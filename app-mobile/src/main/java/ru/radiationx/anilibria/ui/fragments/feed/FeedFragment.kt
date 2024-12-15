package ru.radiationx.anilibria.ui.fragments.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.search.SearchUtils
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.search.FastSearchAdapter
import ru.radiationx.anilibria.ui.fragments.search.FastSearchViewModel
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.showWithLifecycle


/* Created by radiationx on 05.11.17. */

class FeedFragment :
    BaseToolbarFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
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
        }, releaseLongClickListener = { releaseItem, _ ->
            releaseOnLongClick(releaseItem)
        }, youtubeClickListener = { youtubeItem, _ ->
            viewModel.onYoutubeClick(youtubeItem)
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
        localClickListener = { searchViewModel.onLocalItemClick(it) }
    )

    private val viewModel by viewModel<FeedViewModel>()

    private val searchViewModel by viewModel<FastSearchViewModel>()

    private var _searchView: SearchView? = null
    private val searchView: SearchView
        get() = requireNotNull(_searchView)

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _searchView = SearchView(baseBinding.coordinatorLayout.context).apply {
            id = R.id.top_search_view
        }
        baseBinding.coordinatorLayout.addView(searchView)
        searchView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            width = CoordinatorLayout.LayoutParams.MATCH_PARENT
            height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
        }

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
            /*menu.add("Поиск")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener {
                        searchView?.open(true, it)
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)*/


        }

        FeedToolbarShadowController(
            binding.recyclerView,
            baseBinding.appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        searchView.apply {
            setTextHint("Поиск по названию")
            navigationIconSupport = SearchUtils.NavigationIconSupport.SEARCH
            setOnFocusChangeListener(object : SearchLayout.OnFocusChangeListener {
                override fun onFocusChange(hasFocus: Boolean) {
                    if (hasFocus) {
                        navigationIconSupport = SearchUtils.NavigationIconSupport.ARROW
                        viewModel.onFastSearchOpen()
                        baseBinding.appbarLayout.setExpanded(true)
                    } else {
                        navigationIconSupport = SearchUtils.NavigationIconSupport.SEARCH
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

            setAdapter(searchAdapter)
        }

        viewModel.state.onEach { state ->
            binding.progressBarList.isVisible = state.data.emptyLoading
            binding.refreshLayout.isRefreshing = state.data.refreshLoading
            adapter.bindState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        searchViewModel.state.onEach { state ->
            searchAdapter.bindItems(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        searchView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            leftMargin = dimensions.left
            topMargin = dimensions.top
            rightMargin = dimensions.right
        }
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
        searchView.setAdapter(null)
        _searchView = null
    }

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
        baseBinding.appbarLayout.setExpanded(true, true)
    }

    private fun releaseOnLongClick(item: ReleaseItemState) {
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
    }
}
