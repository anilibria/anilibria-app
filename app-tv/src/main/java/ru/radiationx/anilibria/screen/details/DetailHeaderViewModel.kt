package ru.radiationx.anilibria.screen.details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.DetailDataConverter
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.LibriaDetailsRow
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.AuthGuidedScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.PlayerScreen
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class DetailHeaderViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val converter: DetailDataConverter,
    private val router: Router,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    var releaseId: Int = -1

    val releaseData = MutableLiveData<LibriaDetails>()

    private var currentRelease: ReleaseItem? = null

    override fun onCreate() {
        super.onCreate()

        (releaseInteractor.getFull(releaseId) ?: releaseInteractor.getItem(releaseId))?.also {
            update(it)
        }

        releaseInteractor
            .observeFull(releaseId)
            //.delay(2000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                Log.e("kekeke", "observeFull")
                currentRelease = it
                update(it)
            }

    }

    fun onContinueClick() {
        releaseInteractor.getEpisodes(releaseId).maxBy { it.lastAccess }?.also {
            router.navigateTo(PlayerScreen(releaseId, it.id))
        }
    }

    fun onPlayClick() {
        router.navigateTo(PlayerScreen(releaseId))
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

        source
            .lifeSubscribe({
                release.favoriteInfo.isAdded = it.favoriteInfo.isAdded
                release.favoriteInfo.rating = it.favoriteInfo.rating
                update(release)
            }, {
                it.printStackTrace()
            })
    }

    fun onDescriptionClick() {

    }

    private fun update(releaseItem: ReleaseItem) {
        releaseData.value = converter.toDetail(releaseItem)
    }
}