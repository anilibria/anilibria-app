package ru.radiationx.anilibria.presentation.article.details

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.repository.ArticleRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.presentation.ErrorHandler
import ru.radiationx.anilibria.presentation.LinkHandler
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 20.12.17.
 */
@InjectViewState
class ArticlePresenter(
        private val articleRepository: ArticleRepository,
        private val vitalRepository: VitalRepository,
        private val router: Router,
        private val linkHandler: LinkHandler,
        private val errorHandler: ErrorHandler
) : BasePresenter<ArticleView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var currentPageComment = START_PAGE
    var articleId = -1
    var articleIdCode: String = ""
    var currentData: ArticleItem? = null

    fun setDataFromItem(item: ArticleItem) {
        item.run {
            viewState.preShow(imageUrl, title, userNick, commentsCount, viewsCount)
        }
        articleIdCode = item.code
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach " + this)
        loadArticle(articleIdCode)
        loadVital()
    }

    private fun loadVital() {
        vitalRepository
                .observeByRule(VitalItem.Rule.ARTICLE_DETAIL)
                .subscribe {
                    //router.showSystemMessage("Show vital in ART_DETAIL: ${it.size}")
                }
                .addToDisposable()
    }

    private fun loadArticle(code: String) {
        Log.e("S_DEF_LOG", "load article $code")
        viewState.setRefreshing(true)
        articleRepository
                .getArticle(code)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .subscribe({ article ->
                    currentData = article
                    articleId = article.id
                    viewState.showArticle(article)
                    viewState.setRefreshing(false)
                    loadComments(currentPageComment)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun loadComments(page: Int) {
        currentPageComment = page
        articleRepository
                .getComments(articleId, currentPageComment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ comments ->
                    viewState.setEndlessComments(!comments.isEnd())
                    Log.e("S_DEF_LOG", "Comments loaded: " + comments.data.size)
                    comments.data.forEach {
                        Log.e("S_DEF_LOG", "Comment: ${it.id}, ${it.authorNick}")
                    }
                    if (isFirstPage()) {
                        viewState.showComments(comments.data)
                    } else {
                        viewState.insertMoreComments(comments.data)
                    }
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun isFirstPage(): Boolean {
        return currentPageComment == START_PAGE
    }

    fun loadMoreComments() {
        loadComments(currentPageComment + 1)
    }

    fun onShareClick() {
        currentData?.url?.let {
            viewState.share(it)
        }
    }

    fun onCopyLinkClick() {
        currentData?.url?.let {
            viewState.copyLink(it)
        }
    }

    fun onClickLink(url: String): Boolean {
        return linkHandler.handle(url, router)
    }
}
