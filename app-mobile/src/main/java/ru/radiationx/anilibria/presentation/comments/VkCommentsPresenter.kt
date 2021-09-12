package ru.radiationx.anilibria.presentation.comments

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
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
import ru.radiationx.data.entity.app.page.VkComments
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.PageRepository
import ru.terrakok.cicerone.Router
import java.util.concurrent.TimeUnit
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
    private var authRequestDisposable = Disposables.disposed()

    private var hasJsError = false
    private var jsErrorClosed = false

    private var hasVkBlockedError = false
    private var vkBlockedErrorClosed = false

    private val loadingController = DataLoadingController {
        getDataSource().map { ScreenStateAction.Data(it, false) }
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
            .subscribe { viewState.pageReloadAction() }
            .addToDisposable()

        authHolder.observeVkAuthChange()
            .subscribe { viewState.pageReloadAction() }
            .addToDisposable()

        stateController
            .observeState()
            .subscribe { viewState.showState(it) }
            .addToDisposable()

        loadingController
            .observeState()
            .subscribe { loadingData ->
                stateController.updateState {
                    it.copy(data = loadingData)
                }
            }
            .addToDisposable()

        loadingController
            .observeState()
            .filter { it.data != null }
            .map { it.data!! }
            .distinctUntilChanged { t1, t2 ->
                t1 == t2 && vkBlockedErrorClosed
            }
            .debounce(1L, TimeUnit.SECONDS)
            .switchMapSingle { pageRepository.checkVkBlocked() }
            .subscribe { vkBlocked ->
                hasVkBlockedError = vkBlocked
                updateVkBlockedState()
            }
            .addToDisposable()

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
        refresh()
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
        authRequestDisposable.dispose()
        authRequestDisposable = Completable
            .fromAction {
                val url = pendingAuthRequest
                if (isVisibleToUser && url != null) {
                    pendingAuthRequest = null
                    authVkAnalytics.open(AnalyticsConstants.screen_auth_vk)
                    router.navigateTo(Screens.Auth(Screens.AuthVk(url)))
                }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addToDisposable()
    }

    private fun getDataSource(): Single<VkCommentsState> {
        val commentsSource = pageRepository.getComments()
        val releaseSource = Maybe
            .fromCallable<ReleaseItem> {
                releaseInteractor.getItem(releaseId, releaseIdCode)
            }
            .switchIfEmpty(Single.defer<ReleaseItem> {
                releaseInteractor.loadRelease(releaseId, releaseIdCode).firstOrError()
            })

        return Single
            .zip(
                releaseSource,
                commentsSource,
                BiFunction<ReleaseItem, VkComments, VkCommentsState> { result1, result2 ->
                    return@BiFunction VkCommentsState(
                        url = "${result2.baseUrl}release/${result1.code}.html",
                        script = result2.script
                    )
                }
            )
            .doOnError {
                commentsAnalytics.error(it)
                errorHandler.handle(it)
            }
    }
}