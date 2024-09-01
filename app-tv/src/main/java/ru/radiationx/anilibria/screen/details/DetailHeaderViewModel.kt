package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.DetailDataConverter
import ru.radiationx.anilibria.common.DetailsState
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.AuthGuidedScreen
import ru.radiationx.anilibria.screen.DetailOtherGuidedScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.PlayerEpisodesGuidedScreen
import ru.radiationx.anilibria.screen.PlayerScreen
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.FavoritesInteractor
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class DetailHeaderViewModel(
    argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val converter: DetailDataConverter,
    private val router: Router,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
) : LifecycleViewModel() {

    private val releaseId = argExtra.id

    val releaseData = MutableStateFlow<LibriaDetails?>(null)
    val progressState = MutableStateFlow(DetailsState())

    private var currentRelease: Release? = null
    private var isFullLoaded = false

    private var selectEpisodeJob: Job? = null
    private var favoriteDisposable: Job? = null

    init {
        updateProgress()
        releaseInteractor.getItem(releaseId)?.also {
            updateRelease(it, emptyList(), emptySet())
        }

        viewModelScope.launch {
            authRepository.observeAuthState().filter { it == AuthState.AUTH }.first()
            coRunCatching {
                favoritesInteractor.loadReleaseIds()
            }.onFailure {
                Timber.e(it, "Error while favorites release ids")
            }
        }
        combine(
            releaseInteractor.observeFull(releaseId),
            releaseInteractor.observeAccesses(releaseId),
            favoritesInteractor.observeIds()
        ) { release, accesses, favoriteIds ->
            isFullLoaded = true
            updateRelease(release, accesses, favoriteIds)
        }.launchIn(viewModelScope)
    }

    override fun onResume() {
        super.onResume()

        selectEpisodeJob?.cancel()
        selectEpisodeJob = playerController
            .selectEpisodeRelay
            .onEach { episodeId ->
                router.navigateTo(PlayerScreen(releaseId, episodeId))
            }
            .launchIn(viewModelScope)
    }

    override fun onPause() {
        super.onPause()
        selectEpisodeJob?.cancel()
    }

    fun onContinueClick() {
        viewModelScope.launch {
            releaseInteractor.getAccesses(releaseId).maxByOrNull { it.lastAccess }?.also {
                router.navigateTo(PlayerScreen(releaseId, it.id))
            }
        }
    }

    fun onPlayClick() {
        val release = currentRelease ?: return
        if (release.episodes.isEmpty()) return
        if (release.episodes.size == 1) {
            router.navigateTo(PlayerScreen(releaseId, null))
        } else {
            viewModelScope.launch {
                val episodeId =
                    releaseInteractor.getAccesses(releaseId).maxByOrNull { it.lastAccess }?.id
                guidedRouter.open(PlayerEpisodesGuidedScreen(releaseId, episodeId))
            }
        }
    }

    fun onFavoriteClick() {
        val release = currentRelease ?: return

        favoriteDisposable?.cancel()
        favoriteDisposable = viewModelScope.launch {
            if (authRepository.getAuthState() != AuthState.AUTH) {
                guidedRouter.open(AuthGuidedScreen())
                return@launch
            }
            val isAdded = favoritesInteractor.observeIds().first().contains(release.id)
            coRunCatching {
                if (isAdded) {
                    favoriteRepository.deleteRelease(releaseId)
                } else {
                    favoriteRepository.addRelease(releaseId)
                }
            }.onFailure {
                Timber.e(it)
            }
        }.apply {
            invokeOnCompletion { updateProgress() }
        }

        updateProgress()
    }

    fun onDescriptionClick() {

    }

    fun onOtherClick() {
        guidedRouter.open(DetailOtherGuidedScreen(releaseId))
    }

    private fun updateRelease(
        release: Release,
        accesses: List<EpisodeAccess>,
        favoriteIds: Set<ReleaseId>
    ) {
        currentRelease = release
        releaseData.value = converter.toDetail(
            release,
            isFullLoaded,
            accesses,
            favoriteIds.contains(release.id)
        )
        updateProgress()
    }

    private fun updateProgress() {
        progressState.value = DetailsState(
            currentRelease == null,
            currentRelease == null || favoriteDisposable?.isActive ?: false
        )
    }
}