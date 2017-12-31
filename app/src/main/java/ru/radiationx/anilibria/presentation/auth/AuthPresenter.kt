package ru.radiationx.anilibria.presentation.auth

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 30.12.17.
 */
@InjectViewState
class AuthPresenter(private val router: Router,
                    private val authRepository: AuthRepository) : BasePresenter<AuthView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadAuthPage()
    }

    fun loadAuthPage() {
        authRepository.loadAuthPage()
                .subscribe({ urls ->
                    urls.forEachIndexed { index, s ->
                        when (index) {
                            0 -> viewState.setPatreon(s)
                            1 -> viewState.setVk(s)
                        }
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                })
    }

    fun signIn(redirectUrl: String) {
        viewState.setRefreshing(true)
        authRepository.signIn(redirectUrl)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ state ->
                    if (state == AuthState.AUTH) {
                        router.exit()
                    } else {
                        router.showSystemMessage("Что-то пошло не так")
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                })
    }

    fun signIn(login: String, password: String) {
        viewState.setRefreshing(true)
        authRepository.signIn(login, password)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ state ->
                    if (state == AuthState.AUTH) {
                        router.exit()
                    } else {
                        router.showSystemMessage("Что-то пошло не так")
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                })
    }

    fun skip() {
        authRepository.setAuthState(AuthState.AUTH_SKIPPED)
        router.exit()
    }

}