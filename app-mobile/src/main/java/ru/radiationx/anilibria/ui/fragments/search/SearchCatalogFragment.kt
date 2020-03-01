package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchMenuItem
import kotlinx.android.synthetic.main.fragment_list_refresh.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.di.injectDependencies
import ru.radiationx.anilibria.presentation.search.FastSearchPresenter
import ru.radiationx.anilibria.presentation.search.FastSearchView
import ru.radiationx.anilibria.presentation.search.SearchCatalogView
import ru.radiationx.anilibria.presentation.search.SearchPresenter
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.shared.ktx.android.putExtra
import javax.inject.Inject


class SearchCatalogFragment : BaseFragment(), SearchCatalogView, FastSearchView, SharedProvider,
    ReleasesAdapter.ItemListener {

    companion object {
        private const val ARG_GENRE: String = "genre"
        private const val ARG_YEAR: String = "year"

        fun newInstance(
            genres: String? = null,
            years: String? = null
        ) = SearchCatalogFragment().putExtra {
            putString(ARG_GENRE, genres)
            putString(ARG_YEAR, years)
        }
    }

    private lateinit var genresDialog: GenresDialog
    private val adapter = SearchAdapter(
        this, PlaceholderListItem(
            R.drawable.ic_toolbar_search,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_search
        )
    )

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val fastSearchAdapter = FastSearchAdapter {
        //searchView?.close(true)
        searchPresenter.onItemClick(it)
    }.apply {
        setHasStableIds(true)
    }
    private var searchView: SearchMenuItem? = null

    @InjectPresenter
    lateinit var searchPresenter: FastSearchPresenter

    @ProvidePresenter
    fun provideSearchPresenter(): FastSearchPresenter =
        getDependency(FastSearchPresenter::class.java, screenScope)

    @InjectPresenter
    lateinit var presenter: SearchPresenter

    @ProvidePresenter
    fun providePresenter(): SearchPresenter =
        getDependency(SearchPresenter::class.java, screenScope)

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

    override fun getLayoutResource(): Int = R.layout.fragment_list_refresh

    override val statusBarVisible: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView = SearchMenuItem(coordinator_layout.context)
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

                override fun onCheckedSeasons(items: List<String>) {
                    presenter.onChangeSeasons(items)
                }

                override fun onChangeSorting(sorting: String) {
                    presenter.onChangeSorting(sorting)
                }

                override fun onChangeComplete(complete: Boolean) {
                    presenter.onChangeComplete(complete)
                }
            })
        } ?: throw RuntimeException("Burn in hell google! Wtf, why nullable?! Fags...")

        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@SearchCatalogFragment.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
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
                    searchView?.requestFocus(it)
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            add("Фильтры")
                .setIcon(R.drawable.ic_filter_toolbar)
                .setOnMenuItemClickListener {
                    presenter.showDialog()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }


        coordinator_layout.addView(searchView)
        searchView?.layoutParams =
            (searchView?.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams?)?.apply {
                width =
                    androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams.MATCH_PARENT
                height =
                    androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams.WRAP_CONTENT
                behavior = SearchBehavior<SearchMenuItem>()
            }
        (searchView as SearchLayout?)?.apply {
            setTextHint("Название релиза")
            setOnFocusChangeListener(object : SearchLayout.OnFocusChangeListener {
                override fun onFocusChange(hasFocus: Boolean) {
                    if (!hasFocus) {
                        searchPresenter.onClose()
                    }
                }

            })
            setOnQueryTextListener(object : SearchLayout.OnQueryTextListener {
                override fun onQueryTextSubmit(query: CharSequence): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: CharSequence): Boolean {
                    searchPresenter.onQueryChange(newText.toString())
                    return false
                }
            })

            setAdapter(fastSearchAdapter)
        }
    }

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        searchView?.layoutParams =
            (searchView?.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams?)?.apply {
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
            /*if (isProgress) {
                it.showProgress()
            } else {
                it.hideProgress()
            }*/
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

    override fun showSeasons(seasons: List<SeasonItem>) {
        genresDialog.setSeasons(seasons)
    }

    override fun selectGenres(genres: List<String>) {
        genresDialog.setCheckedGenres(genres)
    }

    override fun selectYears(years: List<String>) {
        genresDialog.setCheckedYears(years)
    }

    override fun selectSeasons(seasons: List<String>) {
        genresDialog.setCheckedSeasons(seasons)
    }

    override fun setSorting(sorting: String) {
        genresDialog.setSorting(sorting)
    }

    override fun setComplete(complete: Boolean) {
        genresDialog.setComplete(complete)
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
                .setItems(arrayOf("Добавить на главный экран")) { _, which ->
                    when (which) {
                        0 -> ShortcutHelper.addShortcut(item)
                    }
                }
                .show()
        }
        return false
    }

}
