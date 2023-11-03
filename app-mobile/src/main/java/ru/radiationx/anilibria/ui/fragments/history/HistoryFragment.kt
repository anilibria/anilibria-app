package ru.radiationx.anilibria.ui.fragments.history

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.feed.FeedToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.showWithLifecycle

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryFragment :
    BaseToolbarFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
    SharedProvider,
    ReleasesAdapter.ItemListener {

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    private var searchView: SearchMenuItem? = null

    private val adapter = ReleasesAdapter(
        loadMoreListener = { },
        loadRetryListener = {},
        listener = this,
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
        addDelegate(ReleaseItemDelegate(this@HistoryFragment))
    }

    private val viewModel by viewModel<HistoryViewModel>()

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
                    searchView?.requestFocus(it)
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


        baseBinding.coordinatorLayout.addView(searchView)
        searchView?.layoutParams =
            (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
                width = CoordinatorLayout.LayoutParams.MATCH_PARENT
                height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
                behavior = SearchBehavior<SearchMenuItem>()
            }
        searchView?.apply {
            setTextHint("Название релиза")
            setOnQueryTextListener(object : SearchLayout.OnQueryTextListener {
                override fun onQueryTextSubmit(query: CharSequence): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: CharSequence): Boolean {
                    viewModel.localSearch(newText.toString())
                    return false
                }
            })

            setAdapter(searchAdapter)
        }

        viewModel.state.onEach {
            showState(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        searchView?.layoutParams =
            (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
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

    override fun onItemClick(item: ReleaseItemState, position: Int) {
        viewModel.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItemState): Boolean {
        val titles =
            arrayOf("Копировать ссылку", "Поделиться", "Добавить на главный экран", "Удалить")
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
                    3 -> viewModel.onDeleteClick(item)
                }
            }
            .showWithLifecycle(viewLifecycleOwner)
        return false
    }

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view
    }

    private fun showState(state: HistoryScreenState) {
        binding.progressBarList.isVisible = state.data.emptyLoading
        binding.refreshLayout.isRefreshing = state.data.refreshLoading
        adapter.bindState(state.data)
        searchAdapter.items = state.searchItems.map { ReleaseListItem(it) }
    }
}