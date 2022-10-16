package ru.radiationx.anilibria.screen.details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import ru.radiationx.anilibria.common.DetailDataConverter
import ru.radiationx.anilibria.common.DetailsState
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.AuthGuidedScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.PlayerEpisodesGuidedScreen
import ru.radiationx.anilibria.screen.PlayerScreen
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DetailHeaderViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val converter: DetailDataConverter,
    private val router: Router,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController
) : LifecycleViewModel() {

    var releaseId: Int = -1

    val releaseData = MutableLiveData<LibriaDetails>()
    val progressState = MutableLiveData<DetailsState>()

    private var currentRelease: ReleaseItem? = null

    private var selectEpisodeDisposable = Disposables.disposed()
    private var favoriteDisposable = Disposables.disposed()

    override fun onCreate() {
        super.onCreate()

        (releaseInteractor.getFull(releaseId) ?: releaseInteractor.getItem(releaseId))?.also {
            currentRelease = it
            update(it)
        }
        updateProgress()

        releaseInteractor
            .observeFull(releaseId)
            //.delay(2000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                currentRelease = it
                update(it)
                updateProgress()
            }
    }

    override fun onResume() {
        super.onResume()

        selectEpisodeDisposable.dispose()
        selectEpisodeDisposable = playerController
            .selectEpisodeRelay
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe { episodeId ->
                router.navigateTo(PlayerScreen(releaseId, episodeId))
            }
    }

    override fun onPause() {
        super.onPause()
        selectEpisodeDisposable.dispose()
    }

    fun onContinueClick() {
        releaseInteractor.getEpisodes(releaseId).maxBy { it.lastAccess }?.also {
            router.navigateTo(PlayerScreen(releaseId, it.id))
        }
    }

    fun onPlayClick() {
        val release = currentRelease as? ReleaseFull ?: return
        if (release.episodes.isEmpty()) return
        if (release.episodes.size == 1) {
            router.navigateTo(PlayerScreen(releaseId))
        } else {
            val episodeId = releaseInteractor.getEpisodes(releaseId).maxBy { it.lastAccess }?.id ?: -1
            guidedRouter.open(PlayerEpisodesGuidedScreen(releaseId, episodeId))
        }
    }

    fun onPlayWebClick() {

    }

    fun onFavoriteClick() {
        val release = currentRelease ?: return
        if (authRepository.getAuthState() != AuthState.AUTH) {
            guidedRouter.open(AuthGuidedScreen())
            return
        }

        val source = if (release.favoriteInfo.isAdded) {
            favoriteRepository.deleteFavorite(releaseId)
        } else {
            favoriteRepository.addFavorite(releaseId)
        }

        favoriteDisposable.dispose()
        favoriteDisposable = source
            .doFinally { updateProgress() }
            .lifeSubscribe({
                release.favoriteInfo.isAdded = it.favoriteInfo.isAdded
                release.favoriteInfo.rating = it.favoriteInfo.rating
                update(release)
            }, {
                it.printStackTrace()
            })

        updateProgress()
    }

    fun onDescriptionClick() {

    }

    private fun updateProgress() {
        progressState.value = DetailsState(
            currentRelease == null,
            currentRelease !is ReleaseFull || !favoriteDisposable.isDisposed
        )
    }

    private fun update(releaseItem: ReleaseItem) {
        releaseData.value = converter.toDetail(releaseItem)
    }
}