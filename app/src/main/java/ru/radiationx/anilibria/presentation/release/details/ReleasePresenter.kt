package ru.radiationx.anilibria.presentation.release.details

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.presentation.LinkHandler
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/* Created by radiationx on 18.11.17. */
@InjectViewState
class ReleasePresenter(
        private val releaseRepository: ReleaseRepository,
        private val vitalRepository: VitalRepository,
        private val router: Router,
        private val linkHandler: LinkHandler
) : BasePresenter<ReleaseView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

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

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach " + this)
        loadRelease()
        loadVital()
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
        val source = when {
            releaseId != -1 -> releaseRepository.getRelease(releaseId)
            releaseIdCode != null -> releaseRepository.getRelease(releaseIdCode!!)
            else -> return
        }
        source
                .subscribe({ release ->
                    releaseId = release.id
                    releaseIdCode = release.idName
                    Log.d("S_DEF_LOG", "subscribe call show")
                    viewState.setRefreshing(false)
                    viewState.showRelease(release)
                    loadComments(currentPageComment)
                    currentData = release
                }) { throwable ->
                    viewState.setRefreshing(false)
                    Log.d("S_DEF_LOG", "SAS")
                    throwable.printStackTrace()
                }
                .addToDisposable()
    }

    private fun loadComments(page: Int) {
        currentPageComment = page
        releaseRepository
                .getComments(releaseId, currentPageComment)
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
                }) { throwable ->
                    throwable.printStackTrace()
                }
                .addToDisposable()
    }

    private fun isFirstPage(): Boolean {
        return currentPageComment == START_PAGE
    }

    fun loadMoreComments() {
        loadComments(currentPageComment + 1)
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

    fun onPlayAllClick() {
        currentData?.let {
            if (it.episodes.isEmpty()) {
                it.moonwalkLink?.let { viewState.playMoonwalk(it) }
            } else {
                viewState.playEpisodes(it)
            }
        }
    }

    fun onPlayEpisodeClick(episode: ReleaseFull.Episode, quality: Int) {
        currentData?.let {
            viewState.playEpisode(it, it.episodes.indexOf(episode), quality)
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
                router.showSystemMessage("Для выполнения действия необходимо авторизоваться")
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
                    }) { throwable ->
                        throwable.printStackTrace()
                    }
                    .addToDisposable()
        }

    }

    fun openSearch(genre: String) {
        val args: Bundle = Bundle().apply {
            putString(SearchFragment.ARG_GENRE, genre)
        }
        router.navigateTo(Screens.RELEASES_SEARCH, args)
    }
}
