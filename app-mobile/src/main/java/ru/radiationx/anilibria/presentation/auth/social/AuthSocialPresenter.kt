package ru.radiationx.anilibria.presentation.auth.social

import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.auth.SocialAuthException
import ru.radiationx.data.repository.AuthRepository
import ru.terrakok.cicerone.Router
import java.util.regex.Pattern
import javax.inject.Inject

@InjectViewState
class AuthSocialPresenter @Inject constructor(
        private val authRepository: AuthRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler
) : BasePresenter<AuthSocialView>(router) {

    var argKey: String = ""

    private var currentData: SocialAuth? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        authRepository
                .getSocialAuth(argKey)
                .subscribe({
                    currentData = it
                    viewState.loadPage(it.socialUrl)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }


    fun onNewRedirectLink(url: String?): Boolean {
        currentData?.also { model ->
            try {
                val matcher = Pattern.compile(model.resultPattern).matcher(url)
                if (matcher.find()) {
                    val result = if (matcher.group(1) != null) url else ""
                    //router.exitWithResult(0, result)
                    signSocial(result.orEmpty())
                    return true
                }
            } catch (ignore: Exception) {
            }
        }
        return false
    }

    fun onUserUnderstandWhatToDo() {
        router.exit()
    }

    private fun signSocial(resultUrl: String) {
        currentData?.also { model ->
            authRepository
                    .signInSocial(resultUrl, model)
                    .subscribe({
                        router.finishChain()
                    }, {
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
}