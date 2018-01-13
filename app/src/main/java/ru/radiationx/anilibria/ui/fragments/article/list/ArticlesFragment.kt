package ru.radiationx.anilibria.ui.fragments.article.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.presentation.article.list.ArticlesPresenter
import ru.radiationx.anilibria.presentation.article.list.ArticlesView
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 16.12.17.
 */
open class ArticlesFragment : BaseFragment(), ArticlesView, SharedProvider, ArticlesAdapter.ItemListener {

    open protected val spinnerItems = listOf(
            "" to "Главная",
            "novosti" to "Новости"
    )

    private val adapter = ArticlesAdapter(this)
    lateinit var router: Router

    @InjectPresenter
    lateinit var presenter: ArticlesPresenter

    @ProvidePresenter
    fun provideArticlesPresenter(): ArticlesPresenter {
        return ArticlesPresenter(App.injections.articleRepository,
                (parentFragment as RouterProvider).router)
    }

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun getLayoutResource(): Int = R.layout.fragment_releases

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e("SUKA", "TEST onViewCreated " + this)
        refreshLayout.setOnRefreshListener { presenter.refresh() }

        recyclerView.apply {
            adapter = this@ArticlesFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }


        /*toolbar.apply {
            title = getString(R.string.fragment_title_news)
        }*/

        spinner.apply {
            spinnerContainer.visibility = View.VISIBLE

            adapter = ArrayAdapter<String>(
                    spinner.context,
                    R.layout.item_view_spinner,
                    spinnerItems.map { it.second }
            )
            (adapter as ArrayAdapter<*>).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    Log.e("SUKA", "TEST onItemSelected " + p2)
                    presenter.loadSubCategory(spinnerItems[p2].first)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
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
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    override fun onItemClick(position: Int, view: View) {
        sharedViewLocal = view
    }

    override fun onItemClick(item: ArticleItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ArticleItem): Boolean {
        return presenter.onItemLongClick(item)
    }
}
