package ru.radiationx.anilibria.presentation.article.list

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.repository.ArticleRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.ui.fragments.article.details.ArticleFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 18.12.17.
 */
@InjectViewState
open class ArticlesPresenter(
        private val articleRepository: ArticleRepository,
        private val vitalRepository: VitalRepository,
        private val router: Router
) : BasePresenter<ArticlesView>(router) {
    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE
    open var category = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach")
        refresh()
        loadVital()
    }

    private fun loadVital() {
        vitalRepository
                .observeByRule(VitalItem.Rule.ARTICLE_LIST)
                .subscribe {
                    it.filter { it.type == VitalItem.VitalType.CONTENT_ITEM }.let {
                        if (it.isNotEmpty()) {
                            viewState.showVitalItems(it)
                        }
                    }
                }
                .addToDisposable()
    }

    fun loadCategory(category: String) {
        if (this.category != category) {
            this.category = category
            refresh()
        }
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    private fun loadPage(page: Int) {
        currentPage = page
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        articleRepository.getArticles(category, /*subCategory, */page)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ releaseItems ->
                    viewState.setEndless(!releaseItems.isEnd())
                    if (isFirstPage()) {
                        viewState.showArticles(releaseItems.data)
                    } else {
                        viewState.insertMore(releaseItems.data)
                    }
                }) { throwable ->
                    throwable.printStackTrace()
                }
                .addToDisposable()
    }

    fun refresh() {
        loadPage(START_PAGE)
    }

    fun loadMore() {
        loadPage(currentPage + 1)
    }

    fun onItemClick(item: ArticleItem) {
        val args = Bundle()
        args.putSerializable(ArticleFragment.ARG_ITEM, item)
        router.navigateTo(Screens.ARTICLE_DETAILS, args)
    }

    fun onItemLongClick(item: ArticleItem): Boolean {
        return false
    }
}
