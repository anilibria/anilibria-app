package ru.radiationx.anilibria.presentation.auth.social

import moxy.InjectViewState
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.auth.social.AuthSocialScreenState
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.auth.SocialAuthException
import ru.radiationx.data.repository.AuthRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class AuthSocialPresenter @Inject constructor(
    private val authRepository: AuthRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val authSocialAnalytics: AuthSocialAnalytics
) : BasePresenter<AuthSocialView>(router) {

    var argKey: String = ""

    private var currentData: SocialAuth? = null

    private val detector = WebAuthSoFastDetector()
    private var currentSuccessUrl: String? = null

    private val stateController = StateController(
        AuthSocialScreenState(
            pageState = WebPageViewState.Loading
        )
    )

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        stateController
            .observeState()
            .subscribe { viewState.showState(it) }
            .addToDisposable()

        resetPage()
    }

    private fun resetPage() {
        authRepository
            .getSocialAuth(argKey)
            .subscribe({
                currentData = it
                detector.loadUrl(it.socialUrl)
                viewState.loadPage(it)
            }, {
                authSocialAnalytics.error(it)
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    fun onClearDataClick() {
        currentSuccessUrl = null
        detector.reset()
        detector.clearCookies()
        resetPage()
        stateController.updateState {
            it.copy(showClearCookies = false)
        }
    }

    fun onContinueClick() {
        stateController.updateState {
            it.copy(showClearCookies = false)
        }
        currentSuccessUrl?.also { signSocial(it) }
    }

    fun submitUseTime(time: Long) {
        authSocialAnalytics.useTime(time)
    }

    fun onSuccessAuthResult(result: String) {
        if (detector.isSoFast()) {
            currentSuccessUrl = result
            stateController.updateState {
                it.copy(showClearCookies = true)
            }
        } else {
            signSocial(result)
        }
    }

    fun onUserUnderstandWhatToDo() {
        router.exit()
    }

    fun sendAnalyticsPageError(error: Exception) {
        authSocialAnalytics.error(error)
    }

    fun onPageStateChanged(pageState: WebPageViewState) {
        stateController.updateState {
            it.copy(pageState = pageState)
        }
    }

    private fun signSocial(resultUrl: String) {
        val model = currentData ?: return

        stateController.updateState {
            it.copy(isAuthProgress = true)
        }
        authRepository
            .signInSocial(resultUrl, model)
            .doFinally {
                stateController.updateState {
                    it.copy(isAuthProgress = true)
                }
            }
            .subscribe({
                authSocialAnalytics.success()
                router.finishChain()
            }, {
                authSocialAnalytics.error(it)
                if (it is SocialAuthException) {
                    viewState.showError()
                } else {
                    errorHandler.handle(it)
                    router.exit()
                }
            })
            .addToDisposable()
    }

}