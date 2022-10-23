package ru.radiationx.anilibria.presentation.comments

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsScreenState
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsState
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.AuthVkAnalytics
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.PageRepository
import ru.terrakok.cicerone.Router
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class VkCommentsPresenter @Inject constructor(
    private val userHolder: UserHolder,
    private val pageRepository: PageRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val authHolder: AuthHolder,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val authVkAnalytics: AuthVkAnalytics,
    private val commentsAnalytics: CommentsAnalytics
) : BasePresenter<VkCommentsView>(router) {

    var releaseId = -1
    var releaseIdCode: String? = null

    private var isVisibleToUser = false
    private var pendingAuthRequest: String? = null
    private var authRequestJob: Job? = null

    private var hasJsError = false
    private var jsErrorClosed = false

    private var hasVkBlockedError = false
    private var vkBlockedErrorClosed = false

    private val loadingController = DataLoadingController(presenterScope) {
        getDataSource().let { ScreenStateAction.Data(it, false) }
    }

    private val stateController = StateController(
        VkCommentsScreenState(
            pageState = WebPageViewState.Loading
        )
    )

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        userHolder
            .observeUser()
            .map { it.authState }
            .distinctUntilChanged()
            .onEach { viewState.pageReloadAction() }
            .launchIn(presenterScope)

        authHolder.observeVkAuthChange()
            .onEach { viewState.pageReloadAction() }
            .launchIn(presenterScope)

        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)

        loadingController
            .observeState()
            .onEach { loadingData ->
                stateController.updateState {
                    it.copy(data = loadingData)
                }
            }
            .launchIn(presenterScope)

        presenterScope.launch {
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
        viewState.pageReloadAction()
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
        stateController.updateState {
            it.copy(pageState = pageState)
        }
    }

    private fun updateJsErrorState() {
        stateController.updateState {
            it.copy(jsErrorVisible = hasJsError && !jsErrorClosed)
        }
    }

    private fun updateVkBlockedState() {
        stateController.updateState {
            it.copy(vkBlockedVisible = hasVkBlockedError && !vkBlockedErrorClosed)
        }
    }

    private fun tryExecutePendingAuthRequest() {
        authRequestJob?.cancel()
        authRequestJob = presenterScope.launch {
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
        val releaseSource = releaseInteractor.observeFull(releaseId, releaseIdCode)
        return try {
            combine(releaseSource, commentsSource) { release, comments ->
                VkCommentsState(
                    url = "${comments.baseUrl}release/${release.code}.html",
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