package ru.radiationx.anilibria.ui.fragments.feed

import android.os.Build
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.anilibria.extension.*
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.anilibria.presentation.feed.FeedPresenter
import ru.radiationx.anilibria.presentation.feed.FeedView
import ru.radiationx.anilibria.presentation.search.FastSearchPresenter
import ru.radiationx.anilibria.presentation.search.FastSearchView
import ru.radiationx.anilibria.ui.adapters.PlaceholderDelegate
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.fragments.search.FastSearchAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import javax.inject.Inject
import android.util.TypedValue
import com.lapism.searchview.SearchEditText
import ru.radiationx.shared.ktx.android.inflate
import ru.radiationx.shared.ktx.android.invisible
import ru.radiationx.shared.ktx.android.visible


/* Created by radiationx on 05.11.17. */

class FeedFragment : BaseFragment(), SharedProvider, FeedView, FastSearchView {

    private val adapter = FeedAdapter({
        presenter.loadMore()
    }, schedulesClickListener = {
        presenter.onSchedulesClick()
    }, randomClickListener = {
        presenter.onRandomClick()
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
            //itemAnimator = null
        }

        toolbar.apply {
            title = getString(R.string.fragment_title_releases)
            title = "Лента"
            /*menu.add("Поиск")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener {
                        searchView?.open(true, it)
                        false
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)*/

            layoutParams = layoutParams.apply {
                val tv = TypedValue()
                if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)

                    height = actionBarHeight + dpToPx(8)
                }
            }
        }

        FeedToolbarShadowController(
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
            //isFocusableInTouchMode = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                z = 16f
            }
            setNavigationIcon(R.drawable.ic_toolbar_search)
            close(true)
            setVoice(false)
            setShadow(false)
            setDivider(false)
            setTheme(when (appThemeHolder.getTheme()) {
                AppThemeHolder.AppTheme.LIGHT -> SearchView.THEME_LIGHT
                AppThemeHolder.AppTheme.DARK -> SearchView.THEME_DARK
            })
            shouldClearOnClose = true
            version = SearchView.VERSION_TOOLBAR
            setVersionMargins(SearchView.VERSION_MARGINS_TOOLBAR_BIG)

            hint = "Поиск по названию"

            setOnOpenCloseListener(object : SearchView.OnOpenCloseListener {
                override fun onOpen(): Boolean {
                    showSuggestions()
                    setShadow(true)
                    return true
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

            val cardview = findViewById<CardView>(com.lapism.searchview.R.id.cardView)
            cardview.apply {
                radius = dpToPx(8).toFloat()
                cardElevation = dpToPx(2).toFloat()
                setCardBackgroundColor(context.getColorFromAttr(R.attr.cardBackground))
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    marginStart = dpToPx(16)
                    marginEnd = dpToPx(16)
                    bottomMargin = dpToPx(8)
                }
            }


            val searchEditText = findViewById<SearchEditText>(com.lapism.searchview.R.id.searchEditText_input)
            searchEditText.apply {
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    marginStart = dpToPx(12)
                }
            }
        }
        //searchView?.open(false)
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

    override fun showSchedules(title: String, items: List<ScheduleItem>) {
        adapter.bindSchedules(title, items)
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
                R.drawable.ic_newspaper,
                R.string.placeholder_title_nodata_base,
                R.string.placeholder_desc_nodata_base
        )
        placeHolderContainer.visible(show)
        Log.d("nonono", "showEmptyView $show")
    }

    override fun showEmptyError(show: Boolean, message: String?) {
        Log.d("nonono", "showEmptyError $show, $message")
        placeHolder.bind(
                R.drawable.ic_newspaper,
                R.string.placeholder_title_errordata_base,
                R.string.placeholder_desc_nodata_base
        )
        placeHolderContainer.visible(show)
    }

    override fun showProjects(show: Boolean, items: List<FeedItem>) {
        Log.d("nonono", "showProjects $show, ${items.size}")
        recyclerView.invisible(!show)
        adapter.bindItems(items)
    }

    override fun updateItems(items: List<FeedItem>) {
        adapter.updateItems(items)
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
