package ru.radiationx.anilibria.presentation.comments

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.*
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.presentation.LinkHandler
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.radiationx.anilibria.ui.navigation.AppRouter

@InjectViewState
class CommentsPresenter(
        private val releaseRepository: ReleaseRepository,
        private val commentsRepository: CommentsRepository,
        private val releaseInteractor: ReleaseInteractor,
        private val historyRepository: HistoryRepository,
        private val authRepository: AuthRepository,
        private val router: AppRouter,
        private val linkHandler: LinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<CommentsView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var lasCommentSentTime = 0L

    private var currentPageComment = START_PAGE

    var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach " + this)
        loadRelease()
        loadComments(currentPageComment)
        subscribeAuth()
    }

    private var currentAuthState = authRepository.getAuthState()

    private fun subscribeAuth() {
        authRepository
                .observeUser()
                .subscribe {
                    if (currentAuthState != it.authState) {
                        currentAuthState = it.authState
                        loadRelease()
                    }
                }
                .addToDisposable()
    }

    private fun loadRelease() {
        Log.e("S_DEF_LOG", "load release $releaseId : $releaseIdCode : $currentData")
        /*releaseInteractor
                .observeRelease(releaseId, releaseIdCode)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .subscribe({ release ->
                    if (releaseId != release.id) {
                        releaseId = release.id
                        loadComments(currentPageComment)
                    }
                    releaseIdCode = release.code
                    Log.d("S_DEF_LOG", "subscribe call show")
                    currentData = release
                    historyRepository.putRelease(release as ReleaseItem)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()*/
    }

    private fun loadComments(page: Int) {
        if (releaseId == -1) {
            return
        }
        currentPageComment = page
        commentsRepository
                .getCommentsRelease(releaseId, currentPageComment)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
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

    fun reloadComments() {
        loadComments(1)
    }

    fun markEpisodeViewed(episode: ReleaseFull.Episode) {
        episode.isViewed = true
        releaseInteractor.putEpisode(episode)
    }


    fun onCommentClick(item: Comment) {
        viewState.addCommentText("[USER=${item.authorId}]${item.authorNick}[/USER], ")
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
        currentData?.let { release ->
            commentsRepository
                    .sendCommentRelease(release.code.orEmpty(), release.id, text, "")
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

    fun openAuth() {
        router.navigateTo(Screens.Auth())
    }
}