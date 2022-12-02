package ru.radiationx.anilibria.ui.fragments.youtube

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.presentation.youtube.YoutubeViewModel
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.quill.viewModel

class YoutubeFragment : BaseFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh) {

    private val youtubeAdapter: YoutubeAdapter by lazy {
        YoutubeAdapter(
            loadMoreListener = { viewModel.loadMore() },
            loadRetryListener = { viewModel.loadMore() },
            listener = adapterListener,
            emptyPlaceHolder = PlaceholderListItem(
                R.drawable.ic_youtube,
                R.string.placeholder_title_nodata_base,
                R.string.placeholder_desc_nodata_base
            ),
            errorPlaceHolder = PlaceholderListItem(
                R.drawable.ic_youtube,
                R.string.placeholder_title_errordata_base,
                R.string.placeholder_desc_nodata_base
            )
        )
    }

    private val viewModel by viewModel<YoutubeViewModel>()

    override val statusBarVisible: Boolean = true

    override fun onCreateBinding(view: View): FragmentListRefreshBinding {
        return FragmentListRefreshBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseBinding.toolbar.apply {
            title = getString(R.string.fragment_title_youtube)
        }

        binding.refreshLayout.setOnRefreshListener { viewModel.refresh() }

        binding.recyclerView.apply {
            adapter = youtubeAdapter
            layoutManager = LinearLayoutManager(context)
            disableItemChangeAnimation()
        }

        ToolbarShadowController(
            binding.recyclerView,
            baseBinding.appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        viewModel.state.onEach {state->
            binding.progressBarList.isVisible = state.data.emptyLoading
            binding.refreshLayout.isRefreshing = state.data.refreshLoading
            youtubeAdapter.bindState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    private val adapterListener = object : YoutubeAdapter.ItemListener {

        override fun onItemClick(item: YoutubeItemState, position: Int) {
            viewModel.onItemClick(item)
        }

        override fun onItemLongClick(item: YoutubeItemState): Boolean {
            return false
        }
    }

}