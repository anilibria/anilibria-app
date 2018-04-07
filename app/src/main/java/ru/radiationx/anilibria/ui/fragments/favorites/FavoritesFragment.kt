package ru.radiationx.anilibria.ui.fragments.favorites

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.favorites.FavoritesPresenter
import ru.radiationx.anilibria.presentation.favorites.FavoritesView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration


/**
 * Created by radiationx on 13.01.18.
 */
class FavoritesFragment : BaseFragment(), SharedProvider, FavoritesView, ReleasesAdapter.ItemListener {
    private val adapter: ReleasesAdapter = ReleasesAdapter(this, PlaceholderListItem(
            R.drawable.ic_fav_border,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_favorites
    ))

    @InjectPresenter
    lateinit var presenter: FavoritesPresenter

    @ProvidePresenter
    fun provideFavoritesPresenter(): FavoritesPresenter {
        return FavoritesPresenter(
                App.injections.releaseRepository,
                (parentFragment as RouterProvider).getRouter(),
                App.injections.errorHandler
        )
    }

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun getLayoutResource(): Int = R.layout.fragment_releases

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ToolbarHelper.fixInsets(toolbar)

        toolbar.apply {
            title = getString(R.string.fragment_title_favorites)
            /*setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)*/
        }

        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@FavoritesFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )
        }

        toolbar.inflateMenu(R.menu.search)
        val searchItem = toolbar.menu.findItem(R.id.action_search)
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = searchItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView.queryHint = "Название"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                presenter.localSearch(newText)
                return false
            }
        })
    }

    private fun closeSearch(): Boolean {
        toolbar?.menu?.findItem(R.id.action_search)?.let {
            if (it.isActionViewExpanded) {
                //(it.actionView as SearchView).setQuery(null, false)
                toolbar.collapseActionView()
                return true
            }
        }
        return false
    }

    override fun onBackPressed(): Boolean {
        if (closeSearch())
            return true
        presenter.onBackPressed()
        return true
    }

    override fun onDestroyView() {
        closeSearch()
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
                    .setItems(arrayOf("Удалить"), { dialog, which ->
                        if (which == 0) {
                            presenter.deleteFav(item.id)
                        }
                    })
                    .show()
        }
        return false
    }
}