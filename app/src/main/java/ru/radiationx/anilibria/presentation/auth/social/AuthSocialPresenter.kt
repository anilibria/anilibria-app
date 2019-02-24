package ru.radiationx.anilibria.presentation.auth.social

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.entity.app.auth.SocialAuth
import ru.radiationx.anilibria.entity.app.auth.SocialAuthException
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router
import java.lang.Exception
import java.util.regex.Pattern

@InjectViewState
class AuthSocialPresenter(
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
                    .signSocial(resultUrl, model)
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