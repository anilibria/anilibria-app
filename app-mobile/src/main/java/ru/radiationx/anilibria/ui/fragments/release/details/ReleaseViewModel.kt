package ru.radiationx.anilibria.ui.fragments.release.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

data class ReleaseExtra(
    val id: ReleaseId?,
    val code: ReleaseCode?,
    val release: Release?
) : QuillExtra

@InjectConstructor
class ReleaseViewModel(
    private val argExtra: ReleaseExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val authRepository: AuthRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val commentsNotifier: ReleaseCommentsNotifier,
    private val commentsAnalytics: CommentsAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : ViewModel() {

    private var currentData: Release? = null

    private val _state = MutableStateFlow(ReleasePagerState())
    val state = _state.asStateFlow()

    val shareAction = EventFlow<String>()
    val copyAction = EventFlow<String>()
    val shortcutAction = EventFlow<Release>()
    val openCommentsAction = commentsNotifier.observe()

    init {
        argExtra.release?.also {
            updateLocalRelease(it)
        }
        releaseInteractor.getItem(argExtra.id, argExtra.code)?.also {
            updateLocalRelease(it)
        }
        observeRelease()
        loadRelease()
        subscribeAuth()
    }

    fun onBackPressed() {
        router.exit()
    }

    private fun subscribeAuth() {
        authRepository
            .observeAuthState()
            .drop(1)
            .onEach { loadRelease() }
            .launchIn(viewModelScope)
    }

    private fun loadRelease() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            coRunCatching {
                releaseInteractor.loadRelease(argExtra.id, argExtra.code)
            }.onSuccess {
                historyRepository.putRelease(it as Release)
            }.onFailure {
                errorHandler.handle(it)
            }
            _state.update { it.copy(loading = false) }
        }
    }

    private fun observeRelease() {
        releaseInteractor
            .observeFull(argExtra.id, argExtra.code)
            .onEach { release ->
                updateLocalRelease(release)
                historyRepository.putRelease(release as Release)
            }
            .launchIn(viewModelScope)
    }

    private fun updateLocalRelease(release: Release) {
        currentData = release

        _state.update {
            it.copy(
                poster = currentData?.poster,
                title = currentData?.let {
                    String.format("%s / %s", release.title, release.titleEng)
                }
            )
        }
    }

    fun onShareClick() {
        currentData?.let {
            releaseAnalytics.share(AnalyticsConstants.screen_release, it.id.id)
        }
        currentData?.link?.let {
            shareAction.set(it)
        }
    }

    fun onCopyLinkClick() {
        currentData?.let {
            releaseAnalytics.copyLink(AnalyticsConstants.screen_release, it.id.id)
        }
        currentData?.link?.let {
            copyAction.set(it)
        }
    }

    fun onShortcutAddClick() {
        currentData?.let {
            releaseAnalytics.shortcut(AnalyticsConstants.screen_release, it.id.id)
            shortcutAction.set(it)
        }
    }

    fun onCommentsSwipe() {
        currentData?.also {
            commentsAnalytics.open(AnalyticsConstants.screen_release, it.id.id)
        }
    }

}
