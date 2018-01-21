package ru.radiationx.anilibria.presentation.article.details

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.model.repository.ArticleRepository
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 20.12.17.
 */
@InjectViewState
class ArticlePresenter(
        private val articleRepository: ArticleRepository,
        private val router: Router
) : BasePresenter<ArticleView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var currentPageComment = START_PAGE
    var url: String = ""

    fun setDataFromItem(item: ArticleItem) {
        item.run {
            viewState.preShow(imageUrl, title, userNick, commentsCount, viewsCount)
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach " + this)
        loadArticle(url)
    }

    fun loadArticle(articleUrl: String) {
        Log.e("SUKA", "load article $articleUrl")
        viewState.setRefreshing(true)
        articleRepository.getArticle(articleUrl)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ article ->
                    viewState.showArticle(article)
                }) { throwable ->
                    throwable.printStackTrace()
                }
                .addToDisposable()
    }

    private fun loadComments(page: Int) {
        currentPageComment = page
        /*articleRepository
                .getComments(releaseId, currentPageComment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ comments ->
                    viewState.setEndlessComments(!comments.isEnd())
                    Log.e("SUKA", "Comments loaded: " + comments.data.size)
                    comments.data.forEach {
                        Log.e("SUKA", "Comment: ${it.id}, ${it.authorNick}")
                    }
                    if (isFirstPage()) {
                        viewState.showComments(comments.data)
                    } else {
                        viewState.insertMoreComments(comments.data)
                    }
                }) { throwable ->
                    throwable.printStackTrace()
                }
                .addToDisposable()*/
    }

    private fun isFirstPage(): Boolean {
        return currentPageComment == START_PAGE
    }

    fun loadMoreComments() {
        loadComments(currentPageComment + 1)
    }
}
