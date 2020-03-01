package ru.radiationx.anilibria.ui.fragments.youtube

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list_refresh.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.di.injectDependencies
import ru.radiationx.anilibria.presentation.youtube.YoutubePresenter
import ru.radiationx.anilibria.presentation.youtube.YoutubeView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.ToolbarShadowController
import ru.radiationx.data.entity.app.youtube.YoutubeItem

class YoutubeFragment : BaseFragment(), YoutubeView {

    private val youtubeAdapter: YoutubeAdapter by lazy {
        YoutubeAdapter(adapterListener, PlaceholderListItem(
                R.drawable.ic_toolbar_search,
                R.string.placeholder_title_nodata_base,
                R.string.placeholder_desc_nodata_base
        ))
    }

    @InjectPresenter
    lateinit var presenter: YoutubePresenter

    @ProvidePresenter
    fun providePresenter(): YoutubePresenter = getDependency(YoutubePresenter::class.java, screenScope)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
    }

    override fun getLayoutResource(): Int = R.layout.fragment_list_refresh

    override val statusBarVisible: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            title = getString(R.string.fragment_title_youtube)
        }

        refreshLayout.setOnRefreshListener { presenter.refresh() }

        recyclerView.apply {
            adapter = youtubeAdapter
            layoutManager = LinearLayoutManager(recyclerView.context)
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
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun showItems(items: List<YoutubeItem>) {
        youtubeAdapter.bindItems(items)
    }

    override fun insertMore(items: List<YoutubeItem>) {
        youtubeAdapter.insertMore(items)
    }

    override fun setEndless(enable: Boolean) {
        youtubeAdapter.endless = enable
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    private val adapterListener = object : YoutubeAdapter.ItemListener {
        override fun onLoadMore() {
            presenter.loadMore()
        }

        override fun onItemClick(item: YoutubeItem, position: Int) {
            presenter.onItemClick(item)
        }

        override fun onItemLongClick(item: YoutubeItem): Boolean {
            presenter.onItemLongClick(item)
            return false
        }

    }

}