package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.DetailDataConverter
import ru.radiationx.anilibria.common.DetailsState
import ru.radiationx.anilibria.common.LibriaCard
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
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel для «шапки» (детальной части экрана),
 * показывающей большое изображение, описание, кнопки «Play», «Продолжить» и т.д.
 *
 * Замечание: чтобы при клике на LinkCard/LoadingCard не было "unresolved reference",
 * добавлены no-op методы onLinkCardClick() / onLoadingCardClick() / onLibriaCardClick().
 */
class DetailHeaderViewModel @Inject constructor(
    argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val converter: DetailDataConverter,
    private val router: Router,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController,
) : LifecycleViewModel() {

    /** Состояние детали для «шапки» (название, описание, постер, кнопки и т.д.) */
    val releaseData = MutableStateFlow<LibriaDetails?>(null)

    /** Отдельный стейт (прогресс, обновление и т.д.) */
    val progressState = MutableStateFlow(DetailsState())

    private var currentRelease: Release? = null
    private var isFullLoaded = false

    private var selectEpisodeJob: Job? = null
    private var favoriteJob: Job? = null

    private val releaseId = argExtra.id

    init {
        updateProgress()
        // Сразу попробуем получить релиз из локального кэша:
        releaseInteractor.getItem(releaseId)?.also {
            updateRelease(it, emptyList())
        }
        // Подписываемся на обновления полного релиза + эпизодов (доступы)
        combine(
            releaseInteractor.observeFull(releaseId),
            releaseInteractor.observeAccesses(releaseId)
        ) { releaseFull, accesses ->
            isFullLoaded = true
            updateRelease(releaseFull, accesses)
        }.launchIn(viewModelScope)
    }

    override fun onResume() {
        super.onResume()
        // Следим за «selectEpisodeRelay»
        selectEpisodeJob?.cancel()
        selectEpisodeJob = playerController
            .selectEpisodeRelay
            .onEach { episodeId ->
                // Переходим сразу на PlayerScreen
                router.navigateTo(PlayerScreen(releaseId, episodeId))
            }
            .launchIn(viewModelScope)
    }

    override fun onPause() {
        super.onPause()
        selectEpisodeJob?.cancel()
    }

    // --------------------------------
    // Основные методы (логика кнопок)
    // --------------------------------

    fun onContinueClick() {
        viewModelScope.launch {
            val accesses = releaseInteractor.getAccesses(releaseId)
            val lastEpisode = accesses.maxByOrNull { it.lastAccessRaw }
            lastEpisode?.also {
                router.navigateTo(PlayerScreen(releaseId, it.id))
            }
        }
    }

    /** Кнопка «Play» */
    fun onPlayClick() {
        val release = currentRelease ?: return
        if (release.episodes.isEmpty()) return

        if (release.episodes.size == 1) {
            router.navigateTo(PlayerScreen(releaseId, null))
        } else {
            // Если серий > 1, откроем «список серий»
            viewModelScope.launch {
                val episodeId = releaseInteractor.getAccesses(releaseId)
                    .maxByOrNull { it.lastAccessRaw }?.id
                guidedRouter.open(PlayerEpisodesGuidedScreen(releaseId, episodeId))
            }
        }
    }

    /** Кнопка «Избранное» */
    fun onFavoriteClick() {
        val release = currentRelease ?: return
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            // Если юзер не авторизован
            if (authRepository.getAuthState() != AuthState.AUTH) {
                guidedRouter.open(AuthGuidedScreen())
                return@launch
            }
            coRunCatching {
                if (release.favoriteInfo.isAdded) {
                    favoriteRepository.deleteFavorite(releaseId)
                } else {
                    favoriteRepository.addFavorite(releaseId)
                }
            }.onSuccess { updatedRelease ->
                // Обновим локальный кэш
                currentRelease?.let { old ->
                    val newData = old.copy(favoriteInfo = updatedRelease.favoriteInfo)
                    releaseInteractor.updateFullCache(newData)
                }
            }.onFailure { Timber.e(it) }
            updateProgress()
        }
        updateProgress()
    }

    /** Кнопка «Описание» */
    fun onDescriptionClick() {
        // Пока заглушка
    }

    /** Кнопка «Другое» (сбросить просмотры, отметить как просмотрено и т.д.) */
    fun onOtherClick() {
        guidedRouter.open(DetailOtherGuidedScreen(releaseId))
    }

    // -----------------------------------
    // No-op методы, если DetailFragment
    // вызывает onLinkCardClick() / onLibriaCardClick()
    // -----------------------------------
    fun onLinkCardClick() { /* no-op */ }
    fun onLoadingCardClick() { /* no-op */ }
    fun onLibriaCardClick(card: LibriaCard) { /* no-op */ }

    // -----------------------------------
    // Вспомогательные приватные методы
    // -----------------------------------
    private fun updateRelease(release: Release, accesses: List<EpisodeAccess>) {
        currentRelease = release
        releaseData.value = converter.toDetail(release, isFullLoaded, accesses)
        updateProgress()
    }

    private fun updateProgress() {
        progressState.value = DetailsState(
            loadingProgress = (currentRelease == null),
            updateProgress = (favoriteJob?.isActive == true)
        )
    }
}
