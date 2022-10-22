package ru.radiationx.anilibria.presentation.auth.vk

import moxy.InjectViewState
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.presentation.auth.social.WebAuthSoFastDetector
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.auth.vk.AuthVkScreenState
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class AuthVkPresenter @Inject constructor(
    private val authHolder: AuthHolder,
    private val router: Router
) : BasePresenter<AuthVkView>(router) {

    private var resultPattern =
        "(\\?act=widget|anilibria\\.tv\\/public\\/vk\\.php\\?code=|vk\\.com\\/widget_comments\\.php)"

    var argUrl: String = ""

    private val detector = WebAuthSoFastDetector()
    private var currentSuccessUrl: String? = null

    private val stateController = StateController(
        AuthVkScreenState(
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
        detector.loadUrl(argUrl)
        viewState.loadPage(argUrl, resultPattern)
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
        currentSuccessUrl?.also { successSignVk(it) }
    }

    fun onSuccessAuthResult(result: String) {
        if (detector.isSoFast()) {
            currentSuccessUrl = result
            stateController.updateState {
                it.copy(showClearCookies = true)
            }
        } else {
            successSignVk(result)
        }
    }

    fun onPageStateChanged(pageState: WebPageViewState) {
        stateController.updateState {
            it.copy(pageState = pageState)
        }
    }

    private fun successSignVk(resultUrl: String) {
        authHolder.changeVkAuth(true)
        router.exit()
    }
}