package ru.radiationx.anilibria.presentation.auth

import moxy.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.AuthMainAnalytics
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.auth.SocialAuth
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
    private var currentCode2fa = ""

    private val useTimeCounter = TimeCounter()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        useTimeCounter.start()

        authRepository
                .loadSocialAuth()
                .subscribe({}, {
                    errorHandler.handle(it)
                })
                .addToDisposable()

        authRepository
                .observeSocialAuth()
                .subscribe({
                    viewState.showSocial(it)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
        updateButtonState()
    }

    override fun onDestroy() {
        super.onDestroy()
        authMainAnalytics.useTime(useTimeCounter.elapsed())
    }

    fun onSocialClick(item: SocialAuth) {
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

    fun setCode2fa(code2fa: String) {
        currentCode2fa = code2fa
        updateButtonState()
    }

    private fun updateButtonState() {
        val enabled = currentLogin.isNotEmpty() && currentPassword.isNotEmpty()
        viewState.setSignButtonEnabled(enabled)
    }

    fun signIn() {
        authMainAnalytics.loginClick()
        viewState.setRefreshing(true)
        authRepository
                .signIn(currentLogin, currentPassword, currentCode2fa)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ user ->
                    decideWhatToDo(user.authState)
                }, {
                    authMainAnalytics.error(it)
                    errorHandler.handle(it)
                })
                .addToDisposable()
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

    fun registrationToSiteClick(){
        authMainAnalytics.regToSiteClick()
        Utils.externalLink("${apiConfig.siteUrl}/pages/login.php")
    }

}
