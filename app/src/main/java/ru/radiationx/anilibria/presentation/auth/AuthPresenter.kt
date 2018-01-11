package ru.radiationx.anilibria.presentation.auth

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.ui.fragments.auth.AuthSocialFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 30.12.17.
 */
@InjectViewState
class AuthPresenter(private val router: Router,
                    private val authRepository: AuthRepository) : BasePresenter<AuthView>(router) {

    companion object {
        const val SOCIAL_PATREON = "patreon"
        const val SOCIAL_VK = "ic_logo_vk"
    }

    private var socialUrls = mutableMapOf<String, String>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadAuthPage()
    }

    fun socialClick(socialName: String) {
        val socialUrl = socialUrls[socialName]
        socialUrl?.let { url ->
            router.navigateTo(Screens.AUTH_SOCIAL, url)
            router.setResultListener(AuthSocialFragment.RETURN_URL, {
                router.removeResultListener(AuthSocialFragment.RETURN_URL)
                signIn(it as String)
            })
        }
    }

    fun loadAuthPage() {
        authRepository.loadAuthPage()
                .subscribe({ urls ->
                    urls.forEachIndexed { index, s ->
                        val name = when (index) {
                            0 -> SOCIAL_PATREON
                            1 -> SOCIAL_VK
                            else -> null
                        }
                        name?.let { socialUrls.put(it, s) }
                    }
                    viewState.showSocial()
                }, { throwable ->
                    throwable.printStackTrace()
                })
                .addToDisposable()
    }

    fun signIn(redirectUrl: String) {
        viewState.setRefreshing(true)
        authRepository.signIn(redirectUrl)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ user ->
                    decideWhatToDo(user.authState)
                }, { throwable ->
                    throwable.printStackTrace()
                })
                .addToDisposable()
    }

    fun signIn(login: String, password: String) {
        viewState.setRefreshing(true)
        authRepository.signIn(login, password)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ user ->
                    decideWhatToDo(user.authState)
                }, { throwable ->
                    throwable.printStackTrace()
                })
                .addToDisposable()
    }

    private fun decideWhatToDo(state: AuthState) {
        if (state == AuthState.AUTH) {
            router.replaceScreen(Screens.MAIN)
        } else {
            router.showSystemMessage("Что-то пошло не так")
        }
    }

    fun skip() {
        authRepository.updateUser(AuthState.AUTH_SKIPPED)
        router.replaceScreen(Screens.MAIN)
    }

}
