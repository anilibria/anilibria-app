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
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.youtubeItemDialog
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.quill.viewModel

class YoutubeFragment :
    BaseToolbarFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
    TopScroller {

    private val youtubeAdapter: YoutubeAdapter by lazy {
        YoutubeAdapter(
            loadMoreListener = { viewModel.loadMore() },
            loadRetryListener = { viewModel.loadMore() },
            clickListener = { viewModel.onItemClick(it) },
            longClickListener = { youtubeDialog.show(it) },
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

    private val youtubeDialog by youtubeItemDialog(
        onCopyClick = { viewModel.onCopyClick(it) },
        onShareClick = { viewModel.onShareClick(it) }
    )

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

        viewModel.state.onEach { state ->
            binding.progressBarList.isVisible = state.data.emptyLoading
            binding.refreshLayout.isRefreshing = state.data.refreshLoading
            youtubeAdapter.bindState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
    }

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
        baseBinding.appbarLayout.setExpanded(true, true)
    }

}