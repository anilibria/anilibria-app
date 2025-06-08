package ru.radiationx.anilibria.ui.fragments.search.tab

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.releaseItemDialog
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.search.SearchAdapter
import ru.radiationx.anilibria.ui.fragments.search.controller.SearchShadowController
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.shared.filter.FilterType
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.putExtra

class SearchTabFragment : Fragment(R.layout.fragment_list_refresh), SharedProvider, TopScroller {

    companion object {
        private const val ARG_FILTER_TYPE = "filter_type"
        private const val ARG_COLLECTION_TYPE = "collection_type"

        fun newInstance(
            filterType: FilterType,
            collectionType: CollectionType?
        ): SearchTabFragment {
            return SearchTabFragment().putExtra {
                putSerializable(ARG_FILTER_TYPE, filterType)
                putParcelable(ARG_COLLECTION_TYPE, collectionType)
            }
        }
    }

    private val binding by viewBinding<FragmentListRefreshBinding>()

    private val viewModel by viewModel<SearchTabViewModel> {
        SearchTabExtra(
            type = getExtraNotNull(ARG_FILTER_TYPE),
            collectionType = getExtra(ARG_COLLECTION_TYPE)
        )
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

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postopneEnterTransitionWithTimout()
        binding.recyclerView.doOnLayout {
            startPostponedEnterTransition()
        }

        SearchShadowController(binding.recyclerView) {
            binding.toolbarShadowPrelp.isVisible = it
        }

        binding.refreshLayout.setOnRefreshListener { viewModel.refresh() }

        binding.recyclerView.apply {
            adapter = this@SearchTabFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            disableItemChangeAnimation()
        }

        viewModel.state.onEach { state ->
            binding.progressBarList.isVisible = state.releases.emptyLoading
            binding.refreshLayout.isRefreshing = state.releases.refreshLoading
            adapter.bindState(state)
            if (state.releases.refreshLoading) {
                binding.recyclerView.scrollToPosition(0)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
    }

}