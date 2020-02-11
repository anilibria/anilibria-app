package ru.radiationx.anilibria.ui.fragments.release.list

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import ru.radiationx.anilibria.presentation.release.list.ReleasesPresenter
import ru.radiationx.anilibria.presentation.release.list.ReleasesView
import ru.radiationx.anilibria.presentation.search.FastSearchPresenter
import ru.radiationx.anilibria.presentation.search.FastSearchView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.search.FastSearchAdapter
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.shared.ktx.android.visible
import javax.inject.Inject

/* Created by radiationx on 05.11.17. */

class ReleasesFragment : BaseFragment(), SharedProvider, ReleasesView, FastSearchView,
    ReleasesAdapter.ItemListener {

    private val adapter: ReleasesAdapter = ReleasesAdapter(
        this, PlaceholderListItem(
            R.drawable.ic_releases,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_base
        )
    )

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val searchAdapter = FastSearchAdapter {
        //searchView?.close(true)
        searchPresenter.onItemClick(it)
    }.apply {
        setHasStableIds(true)
    }
    private var searchView: SearchMenuItem? = null

    @InjectPresenter
    lateinit var searchPresenter: FastSearchPresenter

    @InjectPresenter
    lateinit var presenter: ReleasesPresenter

    @ProvidePresenter
    fun provideSearchPresenter(): FastSearchPresenter =
        getDependency(screenScope, FastSearchPresenter::class.java)

    @ProvidePresenter
    fun provideReleasesPresenter(): ReleasesPresenter =
        getDependency(screenScope, ReleasesPresenter::class.java)

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun getLayoutResource(): Int = R.layout.fragment_list_refresh

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("S_DEF_LOG", "TEST onViewCreated $this")
        searchView = SearchMenuItem(coordinator_layout.context)
        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }

        recyclerView.apply {
            adapter = this@ReleasesFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(
                UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )
        }

        toolbar.apply {
            title = getString(R.string.fragment_title_releases)
            menu.add("Поиск")
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

    /* FastSearchView */
    override fun showSearchItems(items: List<SearchItem>) {
        searchAdapter.bindItems(items)
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

    /* ReleaseView */
    override fun showVitalBottom(vital: VitalItem) {
        vitalBottom.visible()
    }

    override fun showVitalItems(vital: List<VitalItem>) {
        adapter.setVitals(vital)
    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
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
        this.sharedViewLocal = view
    }

    override fun onItemClick(item: ReleaseItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItem): Boolean {
        context?.let {
            val titles = arrayOf("Копировать ссылку", "Поделиться", "Добавить на главный экран")
            AlertDialog.Builder(it)
                .setItems(titles) { dialog, which ->
                    when (which) {
                        0 -> {
                            Utils.copyToClipBoard(item.link.orEmpty())
                            Toast.makeText(it, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                        }
                        1 -> Utils.shareText(item.link.orEmpty())
                        2 -> ShortcutHelper.addShortcut(item)
                    }
                }
                .show()
        }
        return false
    }

    /*override fun onItemLongClick(item: ReleaseItem): Boolean {
        return presenter.onItemLongClick(item)
    }*/
}
