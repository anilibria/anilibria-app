package ru.radiationx.anilibria.presentation.article.details

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.repository.ArticleRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.LinkHandler
import ru.radiationx.anilibria.navigation.AppRouter
import ru.radiationx.anilibria.utils.mvp.BasePresenter

/**
 * Created by radiationx on 20.12.17.
 */
@InjectViewState
class ArticlePresenter(
        private val articleRepository: ArticleRepository,
        private val vitalRepository: VitalRepository,
        private val router: AppRouter,
        private val linkHandler: LinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<ArticleView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var lasCommentSentTime = 0L
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
        loadArticle(articleIdCode)
        loadVital()
    }

    private fun loadVital() {
        vitalRepository
                .observeByRule(VitalItem.Rule.ARTICLE_DETAIL)
                .subscribe {
                    //localRouter.showSystemMessage("Show vital in ART_DETAIL: ${it.size}")
                }
                .addToDisposable()
    }

    private fun loadArticle(code: String) {
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
                .doOnTerminate { viewState.setCommentsRefreshing(true) }
                .doAfterTerminate { viewState.setCommentsRefreshing(false) }
                .subscribe({ comments ->
                    viewState.setEndlessComments(!comments.isEnd())
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

    fun reloadComments() {
        loadComments(1)
    }

    fun onClickSendComment(text: String) {
        if (text.length < 3) {
            router.showSystemMessage("Комментарий слишком короткий")
            return
        }
        if ((System.currentTimeMillis() - lasCommentSentTime) < 30000) {
            lasCommentSentTime = System.currentTimeMillis()
            router.showSystemMessage("Комментарий можно отправлять раз в 30 секунд")
            return
        }
        currentData?.let {
            articleRepository
                    .sendComment(it.url.orEmpty(), it.id, text, it.sessId.orEmpty())
                    .subscribe({ comments ->
                        viewState.onCommentSent()
                        currentPageComment = START_PAGE
                        viewState.setEndlessComments(!comments.isEnd())
                        if (isFirstPage()) {
                            viewState.showComments(comments.data)
                        } else {
                            viewState.insertMoreComments(comments.data)
                        }
                    }, {
                        errorHandler.handle(it)
                    })
                    .addToDisposable()
        }
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

    fun onCommentClick(item: Comment) {
        viewState.addCommentText("[USER=${item.authorId}]${item.authorNick}[/USER], ")
    }
}
