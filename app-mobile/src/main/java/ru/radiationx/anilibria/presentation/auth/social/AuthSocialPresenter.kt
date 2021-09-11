package ru.radiationx.anilibria.presentation.auth.social

import android.util.Log
import android.webkit.CookieManager
import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.auth.SocialAuthException
import ru.radiationx.data.repository.AuthRepository
import ru.terrakok.cicerone.Router
import java.util.*
import java.util.concurrent.TimeUnit
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

    private val detector = SoFastDetector()
    private var currentSuccessUrl: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getSocialAuth()
    }

    private fun getSocialAuth() {
        authRepository
            .getSocialAuth(argKey)
            .subscribe({
                currentData = it
                detector.loadUrl(it.socialUrl)
                viewState.loadPage(it.socialUrl)
            }, {
                authSocialAnalytics.error(it)
                errorHandler.handle(it)
            })
            .addToDisposable()
    }

    fun onClearDataClick() {
        currentSuccessUrl = null
        detector.clear()
        CookieManager.getInstance().removeAllCookie()
        getSocialAuth()
    }

    fun onContinueClick() {
        currentSuccessUrl?.also { signSocial(it) }
    }

    fun submitUseTime(time: Long) {
        authSocialAnalytics.useTime(time)
    }

    fun onNewRedirectLink(url: String?): Boolean {
        val cookie = CookieManager.getInstance().getCookie(url)
        Log.d("kekeke", "onNewRedirectLink url $url")
        Log.d("kekeke", "onNewRedirectLink cookie $cookie")
        currentData?.also { model ->
            try {
                val matcher = Pattern.compile(model.resultPattern).matcher(url)
                if (matcher.find()) {
                    val result = if (matcher.group(1) != null) url.orEmpty() else ""
                    if (detector.isSoFast()) {
                        currentSuccessUrl = result
                        viewState.showClearCookies()
                    } else {
                        signSocial(result)
                    }
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

    fun onPageCommitError(error: Exception) {
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

    private class SoFastDetector() {
        private val threshold = TimeUnit.SECONDS.toMillis(15)
        private var hasInitialCookies = false
        private var loadTime: Date? = null

        fun clear() {
            hasInitialCookies = false
            loadTime = null
        }

        fun loadUrl(url: String?) {
            hasInitialCookies = CookieManager.getInstance().getCookie(url) != null
            loadTime = Date()
        }

        fun isSoFast(): Boolean {
            val successTime = Date()
            val isSmallDelta = loadTime?.let {
                val millisDelta = successTime.time - it.time
                millisDelta < threshold
            } ?: false
            return hasInitialCookies && isSmallDelta
        }
    }
}