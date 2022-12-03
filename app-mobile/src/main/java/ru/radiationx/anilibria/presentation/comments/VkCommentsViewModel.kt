package ru.radiationx.anilibria.presentation.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.release.details.ReleaseExtra
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsScreenState
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsState
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.AuthVkAnalytics
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.PageRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.terrakok.cicerone.Router
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class VkCommentsViewModel(
    private val argExtra: ReleaseExtra,
    private val authRepository: AuthRepository,
    private val pageRepository: PageRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val authHolder: AuthHolder,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val authVkAnalytics: AuthVkAnalytics,
    private val commentsAnalytics: CommentsAnalytics
) : ViewModel() {

    private var isVisibleToUser = false
    private var pendingAuthRequest: String? = null
    private var authRequestJob: Job? = null

    private var hasJsError = false
    private var jsErrorClosed = false

    private var hasVkBlockedError = false
    private var vkBlockedErrorClosed = false

    private val loadingController = DataLoadingController(viewModelScope) {
        getDataSource().let { ScreenStateAction.Data(it, false) }
    }

    private val _state = MutableStateFlow(VkCommentsScreenState())
    val state = _state.asStateFlow()

    private val _reloadEvent = EventFlow<Unit>()
    val reloadEvent = _reloadEvent.observe()

    init {
        authRepository
            .observeAuthState()
            .onEach { _reloadEvent.set(Unit) }
            .launchIn(viewModelScope)

        authHolder
            .observeVkAuthChange()
            .onEach { _reloadEvent.set(Unit) }
            .launchIn(viewModelScope)

        loadingController
            .observeState()
            .onEach { loadingData ->
                _state.update { it.copy(data = loadingData) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            runCatching {
                pageRepository
                    .checkVkBlocked()
            }.onSuccess {
                hasVkBlockedError = it
                updateVkBlockedState()
            }.onFailure {
                Timber.e(it)
            }
        }

        loadingController.refresh()
    }


    fun refresh() {
        loadingController.refresh()
    }

    fun pageReload() {
        _reloadEvent.set(Unit)
    }

    fun setVisibleToUser(isVisible: Boolean) {
        isVisibleToUser = isVisible
        tryExecutePendingAuthRequest()
    }

    fun authRequest(url: String) {
        pendingAuthRequest = url
        tryExecutePendingAuthRequest()
    }

    fun onPageLoaded() {
        commentsAnalytics.loaded()
    }

    fun onPageCommitError(error: Exception) {
        commentsAnalytics.error(error)
    }

    fun notifyNewJsError() {
        hasJsError = true
        updateJsErrorState()
    }

    fun closeJsError() {
        jsErrorClosed = true
        updateJsErrorState()
    }

    fun closeVkBlockedError() {
        vkBlockedErrorClosed = true
        updateVkBlockedState()
    }

    fun onNewPageState(pageState: WebPageViewState) {
        _state.update { it.copy(pageState = pageState) }
    }

    private fun updateJsErrorState() {
        _state.update { it.copy(jsErrorVisible = hasJsError && !jsErrorClosed) }
    }

    private fun updateVkBlockedState() {
        _state.update { it.copy(vkBlockedVisible = hasVkBlockedError && !vkBlockedErrorClosed) }
    }

    private fun tryExecutePendingAuthRequest() {
        authRequestJob?.cancel()
        authRequestJob = viewModelScope.launch {
            val url = pendingAuthRequest
            if (isVisibleToUser && url != null) {
                pendingAuthRequest = null
                authVkAnalytics.open(AnalyticsConstants.screen_auth_vk)
                router.navigateTo(Screens.Auth(Screens.AuthVk(url)))
            }
        }
    }

    private suspend fun getDataSource(): VkCommentsState {
        val commentsSource = flow { emit(pageRepository.getComments()) }
        val releaseSource = releaseInteractor.observeFull(argExtra.id, argExtra.code)
        return try {
            combine(releaseSource, commentsSource) { release, comments ->
                VkCommentsState(
                    url = "${comments.baseUrl}release/${release.code.code}.html",
                    script = comments.script
                )
            }.first()
        } catch (ex: Throwable) {
            commentsAnalytics.error(ex)
            errorHandler.handle(ex)
            throw ex
        }
    }
}