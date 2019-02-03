package ru.radiationx.anilibria.ui.fragments.youtube

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.presentation.youtube.YoutubePresenter
import ru.radiationx.anilibria.presentation.youtube.YoutubeView
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration

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
    fun providePresenter(): YoutubePresenter = YoutubePresenter(
            App.injections.youtubeRepository,
            (parentFragment as RouterProvider).getRouter(),
            App.injections.linkHandler,
            App.injections.errorHandler
    )

    override fun getLayoutResource(): Int = R.layout.fragment_releases

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            title = getString(R.string.fragment_title_youtube)
        }

        refreshLayout.setOnRefreshListener { presenter.refresh() }

        recyclerView.apply {
            adapter = youtubeAdapter
            layoutManager = LinearLayoutManager(recyclerView.context)
            addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )
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

    private val adapterListener = object : YoutubeAdapter.ItemListener{
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