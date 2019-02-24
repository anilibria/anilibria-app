package ru.radiationx.anilibria.ui.fragments.history

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
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.history.HistoryPresenter
import ru.radiationx.anilibria.presentation.history.HistoryView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.ToolbarHelper

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryFragment : BaseFragment(), HistoryView, SharedProvider, ReleasesAdapter.ItemListener {

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    private val adapter = ReleasesAdapter(this, PlaceholderListItem(
            R.drawable.ic_history,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_history
    ))

    @InjectPresenter
    lateinit var presenter: HistoryPresenter

    @ProvidePresenter
    fun provideHistoryPresenter(): HistoryPresenter = getDependency(screenScope, HistoryPresenter::class.java)

    override fun getLayoutResource(): Int = R.layout.fragment_list

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarHelper.fixInsets(toolbar)

        toolbar.apply {
            title = "История"
            setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@HistoryFragment.adapter
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

    override fun setRefreshing(refreshing: Boolean) {}

    override fun showReleases(releases: List<ReleaseItem>) {
        adapter.bindItems(releases)
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        //presenter.onItemLongClick(item)
        context?.let {
            AlertDialog.Builder(it)
                    .setItems(arrayOf("Добавить на главный экран", "Удалить")) { _, which ->
                        when (which) {
                            0 -> ShortcutHelper.addShortcut(item)
                            1 -> presenter.onDeleteClick(item)
                        }
                    }
                    .show()
        }
        return false
    }

    override fun onLoadMore() {}

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view
    }

    private fun closeSearch(): Boolean {
        Log.e("lalala", "closeSearch $toolbar \n${toolbar?.menu} \n${toolbar?.menu?.findItem(R.id.action_search)?.isActionViewExpanded}")
        toolbar?.menu?.findItem(R.id.action_search)?.let {
            if (it.isActionViewExpanded) {
                //(it.actionView as SearchView).setQuery(null, false)
                it.collapseActionView()
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
}