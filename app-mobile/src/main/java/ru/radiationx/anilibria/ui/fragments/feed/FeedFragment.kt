package ru.radiationx.anilibria.ui.fragments.feed

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.search.SearchUtils
import com.lapism.search.behavior.SearchBehavior
import com.lapism.search.internal.SearchLayout
import com.lapism.search.widget.SearchView
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.di.injectDependencies
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
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.shared.ktx.android.inflate
import ru.radiationx.shared.ktx.android.invisible
import ru.radiationx.shared.ktx.android.visible
import javax.inject.Inject


/* Created by radiationx on 05.11.17. */

class FeedFragment : BaseFragment(), SharedProvider, FeedView, FastSearchView {

    private val adapter = FeedAdapter({
        presenter.loadMore()
    }, schedulesClickListener = {
        presenter.onSchedulesClick()
    }, scheduleScrollListener = {position->
       presenter.onScheduleScroll(position)
    }, randomClickListener = {
        presenter.onRandomClick()
    }, releaseClickListener = { releaseItem, view ->
        this.sharedViewLocal = view
        presenter.onItemClick(releaseItem)
    }, releaseLongClickListener = { releaseItem, view ->
        releaseOnLongClick(releaseItem)
    }, youtubeClickListener = { youtubeItem, view ->
        presenter.onYoutubeClick(youtubeItem)
    }, scheduleClickListener = { feedScheduleItem, view, position->
        this.sharedViewLocal = view
        presenter.onScheduleItemClick(feedScheduleItem.releaseItem, position)
    })

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val searchAdapter = FastSearchAdapter {
        //searchView?.close(true)

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
    fun provideSearchPresenter(): FastSearchPresenter =
        getDependency(FastSearchPresenter::class.java, screenScope)

    @ProvidePresenter
    fun provideFeedPresenter() = getDependency(FeedPresenter::class.java, screenScope)

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


        }

        FeedToolbarShadowController(
            recyclerView,
            appbarLayout
        ) {
            updateToolbarShadow(it)
        }


        coordinator_layout.addView(searchView)
        searchView?.layoutParams =
            (searchView?.layoutParams as CoordinatorLayout.LayoutParams?)?.apply {
                width =
                    CoordinatorLayout.LayoutParams.MATCH_PARENT
                height =
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT

                behavior = SearchBehavior<SearchView>()
            }
        searchView?.apply {
            setTextHint("Поиск по названию")
            navigationIconSupport = SearchUtils.NavigationIconSupport.SEARCH
            setOnFocusChangeListener(object : SearchLayout.OnFocusChangeListener {
                override fun onFocusChange(hasFocus: Boolean) {
                    if (!hasFocus) {
                        searchPresenter.onClose()
                    }else{
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
            /*if (isProgress) {
                it.showProgress()
            } else {
                it.hideProgress()
            }*/
        }
    }

    /* ReleaseView */

    private val placeHolder by lazy {
        PlaceholderDelegate.ViewHolder(
            placeHolderContainer.inflate(
                R.layout.item_placeholder,
                true
            )
        )
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
