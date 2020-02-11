package ru.radiationx.anilibria.ui.fragments.favorites

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchMenuItem
import kotlinx.android.synthetic.main.fragment_list_refresh.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.getDependency
import ru.radiationx.shared_app.injectDependencies
import ru.radiationx.anilibria.presentation.favorites.FavoritesPresenter
import ru.radiationx.anilibria.presentation.favorites.FavoritesView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import javax.inject.Inject


/**
 * Created by radiationx on 13.01.18.
 */
class FavoritesFragment : BaseFragment(), SharedProvider, FavoritesView,
    ReleasesAdapter.ItemListener {
    private val adapter: ReleasesAdapter = ReleasesAdapter(
        this, PlaceholderListItem(
            R.drawable.ic_fav_border,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_favorites
        )
    )


    private var searchView: SearchMenuItem? = null

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    @InjectPresenter
    lateinit var presenter: FavoritesPresenter

    @ProvidePresenter
    fun provideFavoritesPresenter(): FavoritesPresenter =
        getDependency(screenScope, FavoritesPresenter::class.java)

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
            /*addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )*/
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

            setAdapter(adapter)
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

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showReleases(releases: List<ReleaseItem>) {
        Log.e("S_DEF_LOG", "fav show releases " + releases.size)
        adapter.bindItems(releases)
    }

    override fun insertMore(releases: List<ReleaseItem>) {
        adapter.insertMore(releases)
    }

    override fun removeReleases(releases: List<ReleaseItem>) {
        adapter.removeItems(releases)
    }

    override fun onLoadMore() {
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        context?.let {
            AlertDialog.Builder(it)
                .setItems(arrayOf("Удалить"/*, "Добавить на главный экран"*/)) { dialog, which ->
                    when (which) {
                        0 -> presenter.deleteFav(item.id)
                        1 -> ShortcutHelper.addShortcut(item)
                    }
                }
                .show()
        }
        return false
    }
}