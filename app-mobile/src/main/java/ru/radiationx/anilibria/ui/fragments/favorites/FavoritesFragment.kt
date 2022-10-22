package ru.radiationx.anilibria.ui.fragments.favorites

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchMenuItem
import kotlinx.android.synthetic.main.fragment_list_refresh.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.presentation.favorites.FavoritesPresenter
import ru.radiationx.anilibria.presentation.favorites.FavoritesView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.shared_app.di.injectDependencies


/**
 * Created by radiationx on 13.01.18.
 */
class FavoritesFragment : BaseFragment(), SharedProvider, FavoritesView,
    ReleasesAdapter.ItemListener {

    private val adapter: ReleasesAdapter = ReleasesAdapter(
        loadMoreListener = { presenter.loadMore() },
        loadRetryListener = { presenter.loadMore() },
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

    private var searchView: SearchMenuItem? = null

    @InjectPresenter
    lateinit var presenter: FavoritesPresenter

    @ProvidePresenter
    fun provideFavoritesPresenter(): FavoritesPresenter =
        getDependency(FavoritesPresenter::class.java, screenScope)

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun getLayoutResource(): Int = R.layout.fragment_list_refresh

    override val statusBarVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ToolbarHelper.fixInsets(toolbar)

        searchView = SearchMenuItem(coordinator_layout.context)

        toolbar.apply {
            title = getString(R.string.fragment_title_favorites)
            /*setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)*/
        }

        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@FavoritesFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            disableItemChangeAnimation()
        }

        ToolbarShadowController(
            recyclerView,
            appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        toolbar.menu.apply {
            add("Поиск")
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener {
                    searchView?.requestFocus(it)
                    presenter.onSearchClick()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }


        coordinator_layout.addView(searchView)
        searchView?.layoutParams =
            (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
                width =
                    CoordinatorLayout.LayoutParams.MATCH_PARENT
                height =
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
                behavior = SearchBehavior<SearchMenuItem>()
            }
        searchView?.apply {
            setTextHint("Название релиза")
            setOnQueryTextListener(object : SearchLayout.OnQueryTextListener {
                override fun onQueryTextSubmit(query: CharSequence): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: CharSequence): Boolean {
                    presenter.localSearch(newText.toString())
                    return false
                }
            })

            setAdapter(searchAdapter)
        }
    }

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        searchView?.layoutParams =
            (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
                topMargin = dimensions.statusBar
            }
        searchView?.requestLayout()
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun onDestroyView() {
        searchView?.clearFocus()
        super.onDestroyView()
    }

    override fun showState(state: FavoritesScreenState) {
        progressBarList.isVisible = state.data.emptyLoading
        refreshLayout.isRefreshing = state.data.refreshLoading || state.deletingItemIds.isNotEmpty()
        adapter.bindState(state.data)
        searchAdapter.items = state.searchItems.map { ReleaseListItem(it) }
    }

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view
    }

    override fun onItemClick(item: ReleaseItemState, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItemState): Boolean {
        context?.let {
            val titles =
                arrayOf("Копировать ссылку", "Поделиться", "Добавить на главный экран", "Удалить")
            AlertDialog.Builder(it)
                .setItems(titles) { dialog, which ->
                    when (which) {
                        0 -> {
                            presenter.onCopyClick(item)
                            Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                        }
                        1 -> presenter.onShareClick(item)
                        2 -> presenter.onShortcutClick(item)
                        3 -> presenter.deleteFav(item.id)
                    }
                }
                .show()
        }
        return false
    }
}