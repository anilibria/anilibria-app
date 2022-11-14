package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchMenuItem
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentListRefreshBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.presentation.search.*
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.di.injectDependencies


class SearchCatalogFragment :
    BaseFragment<FragmentListRefreshBinding>(R.layout.fragment_list_refresh), SearchCatalogView,
    FastSearchView, SharedProvider,
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
        loadMoreListener = { presenter.loadMore() },
        loadRetryListener = { presenter.loadMore() },
        listener = this,
        remindCloseListener = { presenter.onRemindClose() },
        emptyPlaceHolder = PlaceholderListItem(
            R.drawable.ic_toolbar_search,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_search
        ),
        errorPlaceHolder = PlaceholderListItem(
            R.drawable.ic_toolbar_search,
            R.string.placeholder_title_errordata_base,
            R.string.placeholder_desc_nodata_base
        )
    )

    private val fastSearchAdapter = FastSearchAdapter(
        clickListener = { searchPresenter.onItemClick(it) },
        localClickListener = { searchPresenter.onLocalItemClick(it) }
    )
    private var searchView: SearchMenuItem? = null

    @InjectPresenter
    lateinit var searchPresenter: FastSearchPresenter

    @ProvidePresenter
    fun provideSearchPresenter(): FastSearchPresenter =
        getDependency(FastSearchPresenter::class.java)

    @InjectPresenter
    lateinit var presenter: SearchPresenter

    @ProvidePresenter
    fun providePresenter(): SearchPresenter =
        getDependency(SearchPresenter::class.java)

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

    override val statusBarVisible: Boolean = true

    override fun onCreateBinding(view: View): FragmentListRefreshBinding {
        return FragmentListRefreshBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView = SearchMenuItem(baseBinding.coordinatorLayout.context)
        genresDialog = context?.let {
            GenresDialog(it, object : GenresDialog.ClickListener {
                override fun onAccept() {
                    presenter.onAcceptDialog()
                }

                override fun onClose() {
                    presenter.onCloseDialog()
                }

                override fun onCheckedGenres(items: List<String>) {
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

        binding.refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        binding.recyclerView.apply {
            adapter = this@SearchCatalogFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            disableItemChangeAnimation()
        }

        ToolbarShadowController(
            binding.recyclerView,
            baseBinding.appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        //ToolbarHelper.fixInsets(toolbar)
        with(baseBinding.toolbar) {
            title = "Поиск"
            /*setNavigationOnClickListener({ presenter.onBackPressed() })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)*/
        }

        baseBinding.toolbar.menu.apply {
            add("Поиск")
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener {
                    presenter.onFastSearchClick()
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


        baseBinding.coordinatorLayout.addView(searchView)
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
                    } else {
                        presenter.onFastSearchOpen()
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

    override fun showState(state: FastSearchScreenState) {
        fastSearchAdapter.bindItems(state)
    }

    override fun showDialog() {
        genresDialog.showDialog()
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
        baseBinding.toolbar.subtitle = subtitle
    }

    override fun showState(state: SearchScreenState) {
        binding.progressBarList.isVisible = state.data.emptyLoading
        binding.refreshLayout.isRefreshing = state.data.refreshLoading
        adapter.bindState(state)
    }

    override fun onItemClick(position: Int, view: View) {
        sharedViewLocal = view
    }

    override fun onItemClick(item: ReleaseItemState, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItemState): Boolean {
        context?.let {
            val titles = arrayOf("Копировать ссылку", "Поделиться", "Добавить на главный экран")
            AlertDialog.Builder(it)
                .setItems(titles) { _, which ->
                    when (which) {
                        0 -> {
                            presenter.onCopyClick(item)
                            Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                        }
                        1 -> presenter.onShareClick(item)
                        2 -> presenter.onShortcutClick(item)
                    }
                }
                .show()
        }
        return false
    }

}
