package ru.radiationx.anilibria.presentation.article.list

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.repository.ArticleRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 18.12.17.
 */
@InjectViewState
open class ArticlesPresenter @Inject constructor(
        private val articleRepository: ArticleRepository,
        private val vitalRepository: VitalRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler
) : BasePresenter<ArticlesView>(router) {
    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE
    open var category = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
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
        articleRepository
                .getArticles(category, /*subCategory, */page)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ articleItems ->
                    viewState.setEndless(!articleItems.isEnd())
                    showData(articleItems.data)
                }) {
                    showData(emptyList())
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun showData(data: List<ArticleItem>) {
        if (isFirstPage()) {
            viewState.showArticles(data)
        } else {
            viewState.insertMore(data)
        }
    }

    fun refresh() {
        loadPage(START_PAGE)
    }

    fun loadMore() {
        loadPage(currentPage + 1)
    }

    fun onItemClick(item: ArticleItem) {
        router.navigateTo(Screens.ArticleDetails(item = item))
    }

    fun onItemLongClick(item: ArticleItem): Boolean {
        return false
    }
}
