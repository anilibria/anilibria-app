package ru.radiationx.anilibria.ui.fragments.search

import android.os.Build
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.lapism.searchview.SearchBehavior
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.release.YearItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.extension.putExtra
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.presentation.search.FastSearchPresenter
import ru.radiationx.anilibria.presentation.search.FastSearchView
import ru.radiationx.anilibria.presentation.search.SearchPresenter
import ru.radiationx.anilibria.presentation.search.SearchView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import javax.inject.Inject


class SearchFragment : BaseFragment(), SearchView, FastSearchView, SharedProvider, ReleasesAdapter.ItemListener {

    companion object {
        private const val ARG_GENRE: String = "genre"
        private const val ARG_YEAR: String = "year"

        fun newInstance(
                genres: String? = null,
                years: String? = null
        ) = SearchFragment().putExtra {
            putString(ARG_GENRE, genres)
            putString(ARG_YEAR, years)
        }
    }

    private lateinit var genresDialog: GenresDialog
    private val adapter = SearchAdapter(this, PlaceholderListItem(
            R.drawable.ic_toolbar_search,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_search
    ))

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val fastSearchAdapter = FastSearchAdapter {
        searchView?.close(true)
        searchPresenter.onItemClick(it)
    }.apply {
        setHasStableIds(true)
    }
    private var searchView: com.lapism.searchview.SearchView? = null

    @InjectPresenter
    lateinit var searchPresenter: FastSearchPresenter

    @ProvidePresenter
    fun provideSearchPresenter(): FastSearchPresenter = getDependency(screenScope, FastSearchPresenter::class.java)

    @InjectPresenter
    lateinit var presenter: SearchPresenter

    @ProvidePresenter
    fun providePresenter(): SearchPresenter = getDependency(screenScope, SearchPresenter::class.java)

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.also { bundle ->
            bundle.getString(ARG_GENRE, null)?.also {
                presenter.onChangeGenres(listOf(it))
            }
            bundle.getString(ARG_YEAR, null)?.also {
                presenter.onChangeYears(listOf(it))
            }
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_releases

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView = com.lapism.searchview.SearchView(coordinator_layout.context)
        genresDialog = context?.let {
            GenresDialog(it, object : GenresDialog.ClickListener {
                override fun onAccept() {
                    presenter.onCloseDialog()
                }

                override fun onCheckedGenres(items: List<String>) {
                    Log.e("lululu", "onCheckedItems ${items.size}")
                    presenter.onChangeGenres(items)
                }

                override fun onCheckedYears(items: List<String>) {
                    presenter.onChangeYears(items)
                }

                override fun onChangeSorting(sorting: String) {
                    presenter.onChangeSorting(sorting)
                }
            })
        } ?: throw RuntimeException("Burn in hell google! Wtf, why nullable?! Fags...")

        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@SearchFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )
        }

        //ToolbarHelper.fixInsets(toolbar)
        with(toolbar) {
            title = "Поиск"
            /*setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)*/
        }

        toolbar.menu.apply {
            add("Поиск")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener {
                        searchView?.open(true, it)
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            add("Settings")
                    .setIcon(R.drawable.ic_filter_toolbar)
                    .setOnMenuItemClickListener {
                        presenter.showDialog()
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }


        coordinator_layout.addView(searchView)
        searchView?.layoutParams = (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
            width = CoordinatorLayout.LayoutParams.MATCH_PARENT
            height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
            behavior = SearchBehavior()
        }
        searchView?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                z = 16f
            }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
            close(false)
            setVoice(false)
            setShadow(true)
            setDivider(true)
            setTheme(when (appThemeHolder.getTheme()) {
                AppThemeHolder.AppTheme.LIGHT -> com.lapism.searchview.SearchView.THEME_LIGHT
                AppThemeHolder.AppTheme.DARK -> com.lapism.searchview.SearchView.THEME_DARK
            })
            shouldClearOnClose = true
            version = com.lapism.searchview.SearchView.VERSION_MENU_ITEM
            setVersionMargins(com.lapism.searchview.SearchView.VERSION_MARGINS_MENU_ITEM)

            hint = "Название релиза"

            setOnOpenCloseListener(object : com.lapism.searchview.SearchView.OnOpenCloseListener {
                override fun onOpen(): Boolean {
                    showSuggestions()
                    return false
                }

                override fun onClose(): Boolean {
                    hideSuggestions()
                    searchPresenter.onClose()
                    return false
                }
            })


            setOnQueryTextListener(object : com.lapism.searchview.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchPresenter.onQueryChange(newText.orEmpty())
                    return false
                }
            })

            adapter = fastSearchAdapter
        }
    }

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        searchView?.layoutParams = (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
            topMargin = dimensions.statusBar
        }
        searchView?.requestLayout()
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun showSearchItems(items: List<SearchItem>) {
        fastSearchAdapter.bindItems(items)
    }

    override fun setSearchProgress(isProgress: Boolean) {
        searchView?.also {
            if (isProgress) {
                it.showProgress()
            } else {
                it.hideProgress()
            }
        }
    }

    override fun showDialog() {
        genresDialog.showDialog()
    }

    override fun showVitalBottom(vital: VitalItem) {

    }

    override fun showVitalItems(vital: List<VitalItem>) {

    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showGenres(genres: List<GenreItem>) {
        genresDialog.setItems(genres)
    }

    override fun showYears(years: List<YearItem>) {
        genresDialog.setYears(years)
    }

    override fun selectGenres(genres: List<String>) {
        genresDialog.setCheckedGenres(genres)
    }

    override fun selectYears(years: List<String>) {
        genresDialog.setCheckedYears(years)
    }

    override fun setSorting(sorting: String) {
        genresDialog.setSorting(sorting)
    }

    override fun updateInfo(sort: String, filters: Int) {
        var subtitle = ""
        subtitle += when (sort) {
            "1" -> "По новизне"
            "2" -> "По популярности"
            else -> "Ваще рандом"
        }
        subtitle += ", Фильтров: $filters"
        toolbar.subtitle = subtitle
    }

    override fun showReleases(releases: List<ReleaseItem>) {
        adapter.bindItems(releases)
    }

    override fun insertMore(releases: List<ReleaseItem>) {
        adapter.insertMore(releases)
    }

    override fun updateReleases(releases: List<ReleaseItem>) {
        adapter.updateItems(releases)
    }

    override fun onLoadMore() {
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    override fun onItemClick(position: Int, view: View) {
        sharedViewLocal = view
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        presenter.onItemLongClick(item)
        context?.let {
            AlertDialog.Builder(it)
                    .setItems(arrayOf("Добавить на главный экран")) { dialog, which ->
                        when (which) {
                            0 -> ShortcutHelper.addShortcut(item)
                        }
                    }
                    .show()
        }
        return false
    }

}
