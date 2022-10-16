package ru.radiationx.anilibria.ui.fragments.history

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchMenuItem
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState
import ru.radiationx.anilibria.presentation.history.HistoryPresenter
import ru.radiationx.anilibria.presentation.history.HistoryView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.feed.FeedToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.shared_app.di.injectDependencies

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

    private val adapter = ReleasesAdapter(
        loadMoreListener = { },
        loadRetryListener = {},
        listener = this,
        emptyPlaceHolder = PlaceholderListItem(
            R.drawable.ic_history,
            R.string.placeholder_title_nodata_base,
            R.string.placeholder_desc_nodata_history
        ),
        errorPlaceHolder = PlaceholderListItem(
            R.drawable.ic_history,
            R.string.placeholder_title_errordata_base,
            R.string.placeholder_desc_nodata_base
        )
    )

    private val searchAdapter = ListItemAdapter().apply {
        addDelegate(ReleaseItemDelegate(this@HistoryFragment))
    }

    @InjectPresenter
    lateinit var presenter: HistoryPresenter

    @ProvidePresenter
    fun provideHistoryPresenter(): HistoryPresenter =
        getDependency(HistoryPresenter::class.java, screenScope)

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
                    presenter.onSearchClick()
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
            disableItemChangeAnimation()
        }


        coordinator_layout.addView(searchView)
        searchView?.layoutParams =
            (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
                width = CoordinatorLayout.LayoutParams.MATCH_PARENT
                height = CoordinatorLayout.LayoutParams.WRAP_CONTENT
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

    override fun showState(state: HistoryScreenState) {
        adapter.bindState(DataLoadingState(data = state.items))
        searchAdapter.items = state.searchItems.map { ReleaseListItem(it) }
    }

    override fun onItemClick(item: ReleaseItemState, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ReleaseItemState): Boolean {
        context?.let {
            val titles =
                arrayOf("Копировать ссылку", "Поделиться", "Добавить на главный экран", "Удалить")
            AlertDialog.Builder(it)
                .setItems(titles) { _, which ->
                    when (which) {
                        0 -> {
                            presenter.onCopyClick(item)
                            Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                        }
                        1 -> presenter.onShareClick(item)
                        2 -> presenter.onShortcutClick(item)
                        3 -> presenter.onDeleteClick(item)
                    }
                }
                .show()
        }
        return false
    }

    override fun onItemClick(position: Int, view: View) {
        this.sharedViewLocal = view
    }
}