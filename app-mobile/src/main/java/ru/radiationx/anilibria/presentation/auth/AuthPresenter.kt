package ru.radiationx.anilibria.presentation.auth

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.system.messages.SystemMessenger
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
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
        private val errorHandler: IErrorHandler
) : BasePresenter<AuthView>(router) {

    private var currentLogin = ""
    private var currentPassword = ""
    private var currentCode2fa = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

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

    fun onSocialClick(item: SocialAuth) {
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
        viewState.setRefreshing(true)
        authRepository
                .signIn(currentLogin, currentPassword, currentCode2fa)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ user ->
                    decideWhatToDo(user.authState)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private fun decideWhatToDo(state: AuthState) {
        if (state == AuthState.AUTH) {
            router.finishChain()
        } else {
            systemMessenger.showMessage("Что-то пошло не так")
        }
    }

    fun skip() {
        authRepository.updateUser(AuthState.AUTH_SKIPPED)
        router.finishChain()
    }

    fun registrationClick() {
        viewState.showRegistrationDialog()
    }

}
