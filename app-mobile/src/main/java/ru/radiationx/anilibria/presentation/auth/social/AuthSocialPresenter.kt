package ru.radiationx.anilibria.presentation.auth.social

import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
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
        private val errorHandler: IErrorHandler,
        private val authSocialAnalytics: AuthSocialAnalytics
) : BasePresenter<AuthSocialView>(router) {

    var argKey: String = ""

    private var currentData: SocialAuth? = null

    private val useTimeCounter = TimeCounter()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        useTimeCounter.start()
        authRepository
                .getSocialAuth(argKey)
                .subscribe({
                    currentData = it
                    viewState.loadPage(it.socialUrl)
                }, {
                    authSocialAnalytics.error(it)
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    override fun onDestroy() {
        super.onDestroy()
        authSocialAnalytics.useTime(useTimeCounter.elapsed())
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
            } catch (ex: Exception) {
                authSocialAnalytics.error(ex)
            }
        }
        return false
    }

    fun onUserUnderstandWhatToDo() {
        router.exit()
    }

    fun onPageCommitError(error:Exception){
        authSocialAnalytics.error(error)
    }

    private fun signSocial(resultUrl: String) {
        currentData?.also { model ->
            authRepository
                    .signInSocial(resultUrl, model)
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
}