package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.releases.ReleaseItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.releases.ReleasesAdapter
import java.util.ArrayList

class SearchFragment : BaseFragment(), SearchView, ReleasesAdapter.ItemListener {

    companion object {
        const val QUERY_TEXT: String = "query"
        const val GENRE: String = "genre"
    }

    override val layoutRes: Int = R.layout.fragment_releases
    private var adapter: ReleasesAdapter = ReleasesAdapter()
    private lateinit var searchMenuItem: MenuItem
    private var currentTitle: String? = "Поиск"

    @InjectPresenter
    lateinit var presenter: SearchPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            presenter.currentQuery = it.getString(QUERY_TEXT, null)
            presenter.currentGenre = it.getString(GENRE, null)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@SearchFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }

        adapter.setListener(this)
        fixToolbarInsets(toolbar)
        with(toolbar) {
            title = currentTitle
            setNavigationOnClickListener({
                App.get().router.exit()
            })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        val card: com.lapism.searchview.SearchView = com.lapism.searchview.SearchView(toolbar.context)
        toolbar.addView(card)
        with(card) {
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
            setOnOpenCloseListener(object : com.lapism.searchview.SearchView.OnOpenCloseListener {
                override fun onOpen(): Boolean {
                    searchMenuItem.isVisible = false
                    toolbar?.navigationIcon = null
                    toolbar?.title = null
                    return false
                }

                override fun onClose(): Boolean {
                    searchMenuItem.isVisible = true
                    toolbar?.setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
                    toolbar?.title = currentTitle
                    return false
                }

            })
            setVoice(false)
            setShadow(false)
            version = com.lapism.searchview.SearchView.Version.MENU_ITEM
            versionMargins = com.lapism.searchview.SearchView.VersionMargins.MENU_ITEM
            setOnQueryTextListener(object : com.lapism.searchview.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        presenter.currentQuery = it
                    }
                    presenter.refreshReleases()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

            })
            hint = "Поиск"
            if (presenter.isEmpty()) {
                open(true)
            }
        }

        with(toolbar.menu) {
            searchMenuItem = add("Search")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener {
                        card.open(true)
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

            add("Settings")
                    .setIcon(R.drawable.ic_toolbar_settings)
                    .setOnMenuItemClickListener {
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showGenres(genres: List<String>) {
    }

    override fun showReleases(releases: ArrayList<ReleaseItem>) {
        if (presenter.currentQuery.orEmpty().isEmpty()) {
            currentTitle = "Поиск"
        } else {
            currentTitle = "Поиск: " + presenter.currentQuery
        }
        toolbar.title = currentTitle
        adapter.bindItems(releases)
    }

    override fun insertMore(releases: ArrayList<ReleaseItem>) {
        adapter.insertMore(releases)
    }

    override fun onLoadMore() {
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    override fun onItemClick(item: ReleaseItem) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        return presenter.onItemLongClick(item)
    }

}
