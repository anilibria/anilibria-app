package ru.radiationx.anilibria.ui.fragments.favorites

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.showWithLifecycle


/**
 * Created by radiationx on 13.01.18.
 */
class FavoritesFragment :
    BaseToolbarFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh),
    SharedProvider,
    ReleasesAdapter.ItemListener,
    TopScroller {

    private val adapter: ReleasesAdapter = ReleasesAdapter(
        loadMoreListener = { viewModel.loadMore() },
        loadRetryListener = { viewModel.loadMore() },
        listener = this,
        emptyPlaceHolder = PlaceholderListItem(
            R.drawable.ic_fav_border,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_favorites
        ),
        errorPlaceHolder = PlaceholderListItem(
            R.drawable.ic_fav_border,
            R.string.placeholder_title_errordata_base,
            R.string.placeholder_desc_nodata_base
        )
    )

    private val searchAdapter = ListItemAdapter().apply {
        addDelegate(ReleaseItemDelegate(this@FavoritesFragment))
    }

    private val viewModel by viewModel<FavoritesViewModel>()

    private var _searchView: SearchMenuItem? = null
    private val searchView: SearchMenuItem
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _searchView = SearchMenuItem(baseBinding.coordinatorLayout.context).apply {
            id = R.id.top_search_view
        }
        baseBinding.coordinatorLayout.addView(searchView)
        searchView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            width = CoordinatorLayout.LayoutParams.MATCH_PARENT
            height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
        }
        super.onViewCreated(view, savedInstanceState)

        //ToolbarHelper.fixInsets(toolbar)
        postopneEnterTransitionWithTimout()
        binding.recyclerView.doOnLayout {
            startPostponedEnterTransition()
        }

        baseBinding.toolbar.apply {
            title = getString(R.string.fragment_title_favorites)
            /*setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)*/
        }

        binding.refreshLayout.setOnRefreshListener { viewModel.refreshReleases() }

        binding.recyclerView.apply {
            adapter = this@FavoritesFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            disableItemChangeAnimation()
        }

        ToolbarShadowController(
            binding.recyclerView,
            baseBinding.appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        baseBinding.toolbar.menu.apply {
            add("Поиск")
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener {
                    searchView.requestFocus(it)
                    viewModel.onSearchClick()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        searchView.apply {
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
        searchView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            leftMargin = dimensions.left
            topMargin = dimensions.top
            rightMargin = dimensions.right
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        searchView.setAdapter(null)
        _searchView = null
    }

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view
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
                    3 -> viewModel.deleteFav(item.id)
                }
            }
            .showWithLifecycle(viewLifecycleOwner)
        return false
    }

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
        baseBinding.appbarLayout.setExpanded(true, true)
    }

    private fun showState(state: FavoritesScreenState) {
        binding.progressBarList.isVisible = state.data.emptyLoading
        binding.refreshLayout.isRefreshing =
            state.data.refreshLoading || state.deletingItemIds.isNotEmpty()
        adapter.bindState(state.data)
        searchAdapter.items = state.searchItems.map { ReleaseListItem(it) }
    }
}