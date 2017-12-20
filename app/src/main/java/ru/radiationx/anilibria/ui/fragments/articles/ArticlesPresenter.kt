package ru.radiationx.anilibria.ui.fragments.articles

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.data.repository.ArticlesRepository
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 18.12.17.
 */
@InjectViewState
open class ArticlesPresenter(private val articlesRepository: ArticlesRepository,
                        private val router: Router) : BasePresenter<ArticlesView>(router) {
    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE
    open var categoryName = Api.Companion.CATEGORY_NEWS

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach")
        refresh()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    private fun loadPage(page: Int) {
        Log.e("SUKA", "loadPage")
        currentPage = page
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        val disposable = articlesRepository.getArticles(categoryName, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ releaseItems ->
                    Log.d("SUKA", "subscribe call show " + releaseItems.current + " : " + releaseItems.allPages + " : " + releaseItems.data.size)
                    viewState.setEndless(!releaseItems.isEnd())
                    if (isFirstPage()) {
                        viewState.setRefreshing(false)
                        viewState.showArticles(releaseItems.data)
                    } else {
                        viewState.insertMore(releaseItems.data)
                    }
                }) { throwable ->
                    viewState.setRefreshing(false)
                    Log.d("SUKA", "SAS")
                    throwable.printStackTrace()
                }
        addDisposable(disposable)
    }

    fun refresh() {
        loadPage(START_PAGE)
    }

    fun loadMore() {
        loadPage(currentPage + 1)
    }

    fun onItemClick(item: ArticleItem) {

    }

    fun onItemLongClick(item: ArticleItem): Boolean {
        return false
    }
}
