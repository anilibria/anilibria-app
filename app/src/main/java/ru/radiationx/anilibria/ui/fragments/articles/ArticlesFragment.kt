package ru.radiationx.anilibria.ui.fragments.articles

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import java.util.ArrayList

/**
 * Created by radiationx on 16.12.17.
 */
class ArticlesFragment : BaseFragment(), ArticlesView, ArticlesAdapter.ItemListener {
    override val layoutRes: Int = R.layout.fragment_releases

    private val adapter = ArticlesAdapter()

    @InjectPresenter
    lateinit var presenter: ArticlesPresenter

    @ProvidePresenter
    fun provideArticlesPresenter(): ArticlesPresenter {
        return ArticlesPresenter(App.injections.articlesRepository,
                (parentFragment as RouterProvider).router)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshLayout.setOnRefreshListener { presenter.refresh() }

        recyclerView.apply {
            adapter = this@ArticlesFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }

        adapter.setListener(this)
        toolbar.apply {
            title = getString(R.string.fragment_title_news)
            /*menu.add("Поиск")
                    .setIcon(R.drawable.ic_toolbar_search)
                    .setOnMenuItemClickListener({
                        presenter.openSearch()
                        //Toast.makeText(context, "Временно не поддерживается", Toast.LENGTH_SHORT).show()
                        false
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)*/
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showArticles(articles: List<ArticleItem>) {
        adapter.bindItems(articles)
    }

    override fun insertMore(articles: List<ArticleItem>) {
        adapter.insertMore(articles)
    }

    override fun onLoadMore() {
        Log.e("SUKA", "onLoadMore")
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    override fun onItemClick(item: ArticleItem) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ArticleItem): Boolean {
        return presenter.onItemLongClick(item)
    }
}