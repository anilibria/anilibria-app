package ru.radiationx.anilibria.presentation.auth

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.model.SocialAuthItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.AuthMainAnalytics
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.auth.EmptyFieldException
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
@InjectViewState
class AuthPresenter @Inject constructor(
    private val router: Router,
    private val systemMessenger: SystemMessenger,
    private val authRepository: AuthRepository,
    private val errorHandler: IErrorHandler,
    private val authMainAnalytics: AuthMainAnalytics,
    private val authSocialAnalytics: AuthSocialAnalytics,
    private val apiConfig: ApiConfig
) : BasePresenter<AuthView>(router) {

    private var currentLogin = ""
    private var currentPassword = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        presenterScope.launch {
            runCatching {
                authRepository.loadSocialAuth()
            }.onFailure {
                errorHandler.handle(it)
            }
        }

        authRepository
            .observeSocialAuth()
            .onEach {
                viewState.showSocial(it.map { it.toState() })
            }
            .launchIn(presenterScope)

        updateButtonState()
    }

    fun onSocialClick(item: SocialAuthItemState) {
        authMainAnalytics.socialClick(item.key)
        authSocialAnalytics.open(AnalyticsConstants.screen_auth_main)
        router.navigateTo(Screens.AuthSocial(item.key))
    }

    fun setLogin(login: String) {
        currentLogin = login
        updateButtonState()
    }

    fun setPassword(password: String) {
        currentPassword = password
        updateButtonState()
    }

    private fun updateButtonState() {
        val enabled = currentLogin.isNotEmpty() && currentPassword.isNotEmpty()
        viewState.setSignButtonEnabled(enabled)
    }

    fun signIn() {
        authMainAnalytics.loginClick()
        presenterScope.launch {
            viewState.setRefreshing(true)
            runCatching {
                authRepository.signIn(currentLogin, currentPassword, "")
            }.onSuccess {
                decideWhatToDo(it.authState)
            }.onFailure {
                if (isEmpty2FaCode(it)) {
                    router.navigateTo(Screens.Auth2FaCode(currentLogin, currentPassword))
                } else {
                    authMainAnalytics.error(it)
                    errorHandler.handle(it)
                }
            }
            viewState.setRefreshing(false)
        }
    }

    private fun isEmpty2FaCode(error: Throwable): Boolean {
        return currentLogin.isNotEmpty()
                && currentPassword.isNotEmpty()
                && error is EmptyFieldException
    }

    private fun decideWhatToDo(state: AuthState) {
        if (state == AuthState.AUTH) {
            authMainAnalytics.success()
            router.finishChain()
        } else {
            authMainAnalytics.wrongSuccess()
            systemMessenger.showMessage("Что-то пошло не так")
        }
    }

    fun skip() {
        authMainAnalytics.skipClick()
        authRepository.updateUser(AuthState.AUTH_SKIPPED)
        router.finishChain()
    }

    fun registrationClick() {
        authMainAnalytics.regClick()
        viewState.showRegistrationDialog()
    }

    fun registrationToSiteClick() {
        authMainAnalytics.regToSiteClick()
        Utils.externalLink("${apiConfig.siteUrl}/pages/login.php")
    }

    fun submitUseTime(time: Long) {
        authMainAnalytics.useTime(time)
    }

}
