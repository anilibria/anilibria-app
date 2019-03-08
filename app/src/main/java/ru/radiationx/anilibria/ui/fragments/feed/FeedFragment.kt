package ru.radiationx.anilibria.ui.fragments.feed

import android.os.Build
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.lapism.searchview.SearchBehavior
import com.lapism.searchview.SearchView
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.entity.app.feed.FeedScheduleItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.extension.inflate
import ru.radiationx.anilibria.extension.invisible
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.presentation.feed.FeedPresenter
import ru.radiationx.anilibria.presentation.feed.FeedView
import ru.radiationx.anilibria.presentation.search.FastSearchPresenter
import ru.radiationx.anilibria.presentation.search.FastSearchView
import ru.radiationx.anilibria.ui.adapters.PlaceholderDelegate
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.anilibria.ui.fragments.search.FastSearchAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import javax.inject.Inject

/* Created by radiationx on 05.11.17. */

class FeedFragment : BaseFragment(), SharedProvider, FeedView, FastSearchView {

    private val adapter = FeedAdapter({
        presenter.loadMore()
    }, releaseClickListener = { releaseItem, view ->
        this.sharedViewLocal = view
        presenter.onItemClick(releaseItem)
    }, releaseLongClickListener = { releaseItem, view ->
        releaseOnLongClick(releaseItem)
    }, youtubeClickListener = { youtubeItem, view ->
        Utils.externalLink(youtubeItem.link)
    }, scheduleClickListener = { feedScheduleItem, view ->
        this.sharedViewLocal = view
        presenter.onItemClick(feedScheduleItem.releaseItem)
    })

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val searchAdapter = FastSearchAdapter {
        searchView?.close(true)
        searchPresenter.onItemClick(it)
    }.apply {
        setHasStableIds(true)
    }
    private var searchView: SearchView? = null

    @InjectPresenter
    lateinit var searchPresenter: FastSearchPresenter

    @InjectPresenter
    lateinit var presenter: FeedPresenter

    @ProvidePresenter
    fun provideSearchPresenter(): FastSearchPresenter = getDependency(screenScope, FastSearchPresenter::class.java)

    @ProvidePresenter
    fun provideFeedPresenter() = getDependency(screenScope, FeedPresenter::class.java)

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun getLayoutResource(): Int = R.layout.fragment_feed

    override val statusBarVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("S_DEF_LOG", "TEST onViewCreated $this")
        searchView = SearchView(coordinator_layout.context)
        refreshLayout.setOnRefreshListener { presenter.refreshReleases() }
        recyclerView.apply {
            adapter = this@FeedFragment.adapter
            layoutManager = LinearLayoutManager(this.context)
            /*addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )*/
            itemAnimator = null
        }

        toolbar.apply {
            title = getString(R.string.fragment_title_releases)
            title = "Лента"
            menu.add("Поиск")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener {
                        searchView?.open(true, it)
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        ToolbarShadowController(
                recyclerView,
                appbarLayout
        ) {
            updateToolbarShadow(it)
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
                AppThemeHolder.AppTheme.LIGHT -> SearchView.THEME_LIGHT
                AppThemeHolder.AppTheme.DARK -> SearchView.THEME_DARK
            })
            shouldClearOnClose = true
            version = SearchView.VERSION_MENU_ITEM
            setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM)

            hint = "Название релиза"

            setOnOpenCloseListener(object : SearchView.OnOpenCloseListener {
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

            adapter = searchAdapter
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        adapter.restoreState(savedInstanceState)
    }

    override fun onDestroyView() {
        adapter.saveState(null)
        super.onDestroyView()
    }

    /* FastSearchView */
    override fun showSearchItems(items: List<SearchItem>) {
        searchAdapter.bindItems(items)
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

    /* ReleaseView */

    private val placeHolder by lazy {
        PlaceholderDelegate.ViewHolder(placeHolderContainer.inflate(R.layout.item_placeholder, true))
    }

    override fun showSchedules(items: List<FeedScheduleItem>) {
        adapter.bindSchedules(items)
    }

    override fun showRefreshProgress(show: Boolean) {
        Log.d("nonono", "showRefreshProgress $show")
        refreshLayout.isRefreshing = show
    }

    override fun showEmptyProgress(show: Boolean) {
        Log.d("nonono", "showEmptyProgress $show")
        progressBarList.visible(show)
        refreshLayout.visible(!show)
        refreshLayout.isRefreshing = false
    }

    override fun showPageProgress(show: Boolean) {
        Log.d("nonono", "showPageProgress $show")
        adapter.showProgress(show)
    }

    override fun showEmptyView(show: Boolean) {
        placeHolder.bind(
                R.drawable.ic_releases,
                R.string.placeholder_title_nodata_base,
                R.string.placeholder_desc_nodata_base
        )
        placeHolderContainer.visible(show)
        Log.d("nonono", "showEmptyView $show")
    }

    override fun showEmptyError(show: Boolean, message: String?) {
        Log.d("nonono", "showEmptyError $show, $message")
        placeHolder.bind(
                R.drawable.ic_logo_patreon,
                R.string.placeholder_title_comments,
                R.string.placeholder_desc_comments
        )
        placeHolderContainer.visible(show)
    }

    override fun showProjects(show: Boolean, items: List<FeedItem>) {
        Log.d("nonono", "showProjects $show, ${items.size}")
        recyclerView.invisible(!show)
        adapter.bindItems(items)
    }

    override fun setRefreshing(refreshing: Boolean) {}

    private fun releaseOnLongClick(item: ReleaseItem) {
        val titles = arrayOf("Копировать ссылку", "Поделиться", "Добавить на главный экран")
        AlertDialog.Builder(context!!)
                .setItems(titles) { dialog, which ->
                    when (which) {
                        0 -> {
                            Utils.copyToClipBoard(item.link.orEmpty())
                            Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                        }
                        1 -> Utils.shareText(item.link.orEmpty())
                        2 -> ShortcutHelper.addShortcut(item)
                    }
                }
                .show()
    }
}
