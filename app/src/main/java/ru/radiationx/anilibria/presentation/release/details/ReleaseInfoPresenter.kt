package ru.radiationx.anilibria.presentation.release.details

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.repository.FavoriteRepository
import ru.radiationx.anilibria.model.repository.HistoryRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.utils.Utils
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class ReleaseInfoPresenter @Inject constructor(
        private val releaseInteractor: ReleaseInteractor,
        private val historyRepository: HistoryRepository,
        private val vitalRepository: VitalRepository,
        private val authRepository: AuthRepository,
        private val favoriteRepository: FavoriteRepository,
        private val router: Router,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler,
        private val apiConfig: ApiConfig
) : BasePresenter<ReleaseInfoView>(router) {

    private var currentData: ReleaseFull? = null
    var releaseId = -1
    var releaseIdCode: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        releaseInteractor.getItem(releaseId, releaseIdCode)?.also {
            updateLocalRelease(ReleaseFull(it))
        }
        observeRelease()
        loadRelease()
        loadVital()
        subscribeAuth()
    }

    fun getQuality() = releaseInteractor.getQuality()

    fun setQuality(value: Int) = releaseInteractor.setQuality(value)

    fun getPlayerType() = releaseInteractor.getPlayerType()

    fun setPlayerType(value: Int) = releaseInteractor.setPlayerType(value)


    private fun subscribeAuth() {
        authRepository
                .observeUser()
                .distinctUntilChanged()
                .skip(1)
                .subscribe {
                    loadRelease()
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
        releaseInteractor
                .loadRelease(releaseId, releaseIdCode)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .subscribe({ release ->
                    viewState.setRefreshing(false)
                    historyRepository.putRelease(release as ReleaseItem)
                }) {
                    viewState.setRefreshing(false)
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun observeRelease() {
        releaseInteractor
                .observeFull(releaseId, releaseIdCode)
                .subscribe({ release ->
                    updateLocalRelease(release)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun updateLocalRelease(release: ReleaseFull) {
        currentData = release
        releaseId = release.id
        releaseIdCode = release.code
        viewState.showRelease(release)
    }

    fun markEpisodeViewed(episode: ReleaseFull.Episode) {
        episode.isViewed = true
        episode.lastAccess = System.currentTimeMillis()
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
                viewState.playWeb(it, release.code.orEmpty())
            }
        }
    }

    fun onPlayAllClick() {
        currentData?.let {
            viewState.playEpisodes(it)
        }
    }

    fun onClickContinue() {
        currentData?.also { release ->
            Log.e("jojojo", release.episodes.joinToString { "${it.id}=>${it.lastAccess}" })
            release.episodes.asReversed().maxBy { it.lastAccess }?.let { episode ->
                viewState.playContinue(release, episode)
            }
        }
    }

    fun onClickEpisodesMenu() {
        currentData?.also { viewState.showEpisodesMenuDialog() }
    }

    fun onPlayEpisodeClick(episode: ReleaseFull.Episode, playFlag: Int? = null, quality: Int? = null) {
        currentData?.let {
            viewState.playEpisode(it, episode, playFlag, quality)
        }
    }

    fun onClickLink(url: String): Boolean {
        return linkHandler.handle(url, router)
    }

    fun onClickDonate() {
        //router.navigateTo(Screens.StaticPage(PageApi.PAGE_ID_DONATE))
        Utils.externalLink("${apiConfig.baseUrl}/${PageApi.PAGE_ID_DONATE}")
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

    fun onScheduleClick(day: Int) {
        router.navigateTo(Screens.Schedule(day))
    }

    fun openAuth() {
        router.navigateTo(Screens.Auth())
    }

    fun openSearch(genre: String) {
        router.navigateTo(Screens.ReleasesSearch(genre))
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
        //router.navigateTo(Screens.StaticPage(PageApi.PAGE_ID_DONATE))
        Utils.externalLink("${apiConfig.baseUrl}/${PageApi.PAGE_ID_DONATE}")
    }

    fun onResetEpisodesHistoryClick() {
        currentData?.also {
            releaseInteractor.resetEpisodesHistory(it.id)
        }
    }

    fun onCheckAllEpisodesHistoryClick() {
        currentData?.also {
            it.episodes.forEach {
                it.isViewed = true
            }
            releaseInteractor.putEpisodes(it.episodes)
        }
    }

}