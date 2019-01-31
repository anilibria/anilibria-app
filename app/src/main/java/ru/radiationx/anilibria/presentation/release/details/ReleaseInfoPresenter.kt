package ru.radiationx.anilibria.presentation.release.details

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.*
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.presentation.LinkHandler
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

@InjectViewState
class ReleaseInfoPresenter(
        private val releaseRepository: ReleaseRepository,
        private val releaseInteractor: ReleaseInteractor,
        private val historyRepository: HistoryRepository,
        private val pageRepository: PageRepository,
        private val vitalRepository: VitalRepository,
        private val authRepository: AuthRepository,
        private val favoriteRepository: FavoriteRepository,
        private val router: Router,
        private val linkHandler: LinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<ReleaseInfoView>(router) {

    var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null
    private var currentAuthState = authRepository.getAuthState()


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach " + this)
        loadRelease()
        //loadComments(currentPageComment)
        loadVital()
        subscribeAuth()
    }


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

    fun getPlayerType() = releaseInteractor.getPlayerType()

    fun setPlayerType(value: Int) = releaseInteractor.setPlayerType(value)


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

    private fun loadVital() {
        vitalRepository
                .observeByRule(VitalItem.Rule.RELEASE_DETAIL)
                .subscribe { vitals ->
                    vitals.filter { it.type == VitalItem.VitalType.CONTENT_ITEM }.let {
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
                    releaseIdCode = release.code
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

    fun markEpisodeViewed(episode: ReleaseFull.Episode) {
        episode.isViewed = true
        releaseInteractor.putEpisode(episode)
    }

    fun onTorrentClick() {
        currentData?.let {
            when {
                it.torrents.size == 1 -> viewState.loadTorrent(it.torrents.last())
                else -> viewState.showTorrentDialog(it.torrents)
            }
        }
    }

    fun onClickWatchWeb() {
        currentData?.let { release ->
            release.moonwalkLink?.let {
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
        currentData?.let { release ->
            release.episodes.maxBy { it.lastAccess }?.let { episode ->
                viewState.playContinue(release, episode)
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
        if (authRepository.getAuthState() != AuthState.AUTH) {
            viewState.showFavoriteDialog()
            return
        }
        val releaseId = currentData?.id ?: return
        val favInfo = currentData?.favoriteInfo ?: return

        val source = if (favInfo.isAdded) {
            favoriteRepository.deleteFavorite(releaseId)
        } else {
            favoriteRepository.addFavorite(releaseId)
        }

        source
                .doOnSubscribe {
                    favInfo.inProgress = true
                    viewState.updateFavCounter()
                }
                .doAfterTerminate {
                    favInfo.inProgress = false
                    viewState.updateFavCounter()
                }
                .subscribe({ releaseItem ->
                    favInfo.rating = releaseItem.favoriteInfo.rating
                    favInfo.isAdded = releaseItem.favoriteInfo.isAdded
                    viewState.updateFavCounter()
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
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

    fun onDownloadLinkSelected(url: String) {
        currentData?.also {
            if (it.showDonateDialog) {
                viewState.showFileDonateDialog(url)
            } else {
                viewState.showDownloadDialog(url)
            }
        }
    }

    fun onDialogPatreonClick() {
        Utils.externalLink("https://www.patreon.com/anilibria")
    }

    fun onDialogDonateClick() {
        router.navigateTo(Screens.STATIC_PAGE, PageApi.PAGE_ID_DONATE)
    }

}