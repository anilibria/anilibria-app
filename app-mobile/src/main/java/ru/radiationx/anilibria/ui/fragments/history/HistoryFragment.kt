package ru.radiationx.anilibria.ui.fragments.history

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchMenuItem
import com.lapism.search.widget.SearchView
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.presentation.history.HistoryPresenter
import ru.radiationx.anilibria.presentation.history.HistoryView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.feed.FeedToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import javax.inject.Inject

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


    private var searchView: SearchMenuItem? = null

    @Inject
    lateinit var appThemeHolder: AppThemeHolder


    private val adapter = ReleasesAdapter(this, PlaceholderListItem(
            R.drawable.ic_history,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_history
    ))

    @InjectPresenter
    lateinit var presenter: HistoryPresenter

    @ProvidePresenter
    fun provideHistoryPresenter(): HistoryPresenter = getDependency(screenScope, HistoryPresenter::class.java)

    override val statusBarVisible: Boolean = true

    override fun getLayoutResource(): Int = R.layout.fragment_list

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = SearchMenuItem(coordinator_layout.context)
        ToolbarHelper.fixInsets(toolbar)

        toolbar.apply {
            title = "История"
            setNavigationOnClickListener { presenter.onBackPressed() }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
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

        FeedToolbarShadowController(
                recyclerView,
                appbarLayout
        ) {
            updateToolbarShadow(it)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = this@HistoryFragment.adapter
            /*addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )*/
        }


        coordinator_layout.addView(searchView)
        searchView?.layoutParams = (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
            width = CoordinatorLayout.LayoutParams.MATCH_PARENT
            height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
            behavior = SearchBehavior<SearchView>()
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
        searchView?.layoutParams = (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
            topMargin = dimensions.statusBar
        }
        searchView?.requestLayout()
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
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

}