package ru.radiationx.anilibria.presentation.release.details

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.repository.HistoryRepository
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.presentation.LinkHandler
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/* Created by radiationx on 18.11.17. */
@InjectViewState
class ReleasePresenter(
        private val releaseRepository: ReleaseRepository,
        private val releaseInteractor: ReleaseInteractor,
        private val historyRepository: HistoryRepository,
        private val vitalRepository: VitalRepository,
        private val authRepository: AuthRepository,
        private val router: Router,
        private val linkHandler: LinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<ReleaseView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var lasCommentSentTime = 0L

    private var currentPageComment = START_PAGE

    var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    fun setCurrentData(item: ReleaseItem) {
        currentData = ReleaseFull(item)
        currentData?.let {
            viewState.showRelease(it)
        }
    }

    fun setLoadedData(data: ReleaseFull) {
        currentData = data
        currentData?.let {
            viewState.showRelease(it)
        }
    }

    fun getQuality() = releaseInteractor.getQuality()

    fun setQuality(value: Int) = releaseInteractor.setQuality(value)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach " + this)
        loadRelease()
        loadComments(currentPageComment)
        loadVital()
        subscribeAuth()
    }

    private var currentAuthState = authRepository.getAuthState()

    private fun subscribeAuth() {
        authRepository
                .observeUser()
                .subscribe({
                    if (currentAuthState != it.authState) {
                        currentAuthState = it.authState
                        loadRelease()
                    }
                })
                .addToDisposable()
    }

    private fun loadVital() {
        vitalRepository
                .observeByRule(VitalItem.Rule.RELEASE_DETAIL)
                .subscribe {
                    it.filter { it.type == VitalItem.VitalType.CONTENT_ITEM }.let {
                        if (it.isNotEmpty()) {
                            viewState.showVitalItems(it)
                        }
                    }
                }
                .addToDisposable()
    }

    private fun loadRelease() {
        Log.e("S_DEF_LOG", "load release $releaseId : $releaseIdCode : $currentData")
        releaseInteractor
                .observeRelease(releaseId, releaseIdCode)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .subscribe({ release ->
                    if (releaseId != release.id) {
                        releaseId = release.id
                        loadComments(currentPageComment)
                    }
                    releaseIdCode = release.idName
                    Log.d("S_DEF_LOG", "subscribe call show")
                    viewState.showRelease(release)
                    viewState.setRefreshing(false)
                    currentData = release
                    historyRepository.putRelease(release as ReleaseItem)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun loadComments(page: Int) {
        if (releaseId == -1) {
            return
        }
        currentPageComment = page
        releaseRepository
                .getComments(releaseId, currentPageComment)
                .doOnTerminate { viewState.setCommentsRefreshing(true) }
                .doAfterTerminate { viewState.setCommentsRefreshing(false) }
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

    fun onTorrentClick() {
        currentData?.let {
            when {
                it.torrents.isEmpty() -> it.torrentLink?.let { url -> viewState.loadTorrent(url) }
                it.torrents.size == 1 -> viewState.loadTorrent(it.torrents.last().url)
                else -> viewState.showTorrentDialog(it.torrents)
            }
        }
    }

    fun onShareClick() {
        Log.e("S_DEF_LOG", "onShareClick $currentData, ${currentData?.link}")
        currentData?.link?.let {
            Log.e("S_DEF_LOG", "onShareClick $it")
            viewState.shareRelease(it)
        }
    }

    fun onCopyLinkClick() {
        Log.e("S_DEF_LOG", "onShareClick $currentData, ${currentData?.link}")
        currentData?.link?.let {
            Log.e("S_DEF_LOG", "onShareClick $it")
            viewState.copyLink(it)
        }
    }

    fun onClickWatchWeb() {
        currentData?.let {
            it.moonwalkLink?.let {
                viewState.playWeb(it)
            }
        }
    }

    fun onPlayAllClick() {
        currentData?.let {
            viewState.playEpisodes(it)
        }
    }

    fun onClickContinue() {
        currentData?.let {
            it.episodes.maxBy { it.lastAccess }?.let { episode ->
                viewState.playContinue(it, episode)
            }
        }
    }

    fun onPlayEpisodeClick(episode: ReleaseFull.Episode, quality: Int? = null) {
        currentData?.let {
            viewState.playEpisode(it, episode, null, quality)
        }
    }

    fun onClickLink(url: String): Boolean {
        return linkHandler.handle(url, router)
    }

    fun onClickDonate() {
        router.navigateTo(Screens.STATIC_PAGE, PageApi.PAGE_ID_DONATE)
    }

    fun onClickFav() {
        currentData?.favoriteCount?.let { fav ->
            if (fav.isGuest) {
                viewState.showFavoriteDialog()
                return
            }
            releaseRepository
                    .sendFav(fav.id, !fav.isFaved, fav.sessId, fav.skey)
                    .doOnSubscribe {
                        fav.inProgress = true
                        viewState.updateFavCounter()
                    }
                    .doAfterTerminate {
                        fav.inProgress = false
                        viewState.updateFavCounter()
                    }
                    .subscribe({ newCount ->
                        fav.count = newCount
                        fav.isFaved = !fav.isFaved
                        viewState.updateFavCounter()
                    }) {
                        errorHandler.handle(it)
                    }
                    .addToDisposable()
        }

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
            releaseRepository
                    .sendComment(it.idName.orEmpty(), it.id, text, it.sessId.orEmpty())
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
        router.navigateTo(Screens.AUTH)
    }

    fun openSearch(genre: String) {
        val args: Bundle = Bundle().apply {
            putString(SearchFragment.ARG_GENRE, genre)
        }
        router.navigateTo(Screens.RELEASES_SEARCH, args)
    }
}
