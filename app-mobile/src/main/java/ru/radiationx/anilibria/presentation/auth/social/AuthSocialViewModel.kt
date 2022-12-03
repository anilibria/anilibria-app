package ru.radiationx.anilibria.presentation.auth.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.auth.social.AuthSocialScreenState
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
import ru.radiationx.data.entity.domain.auth.SocialAuthException
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

data class AuthSocialExtra(
    val key: String
) : QuillExtra

@InjectConstructor
class AuthSocialViewModel(
    private val argExtra: AuthSocialExtra,
    private val authRepository: AuthRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val authSocialAnalytics: AuthSocialAnalytics
) : ViewModel() {

    private val detector = WebAuthSoFastDetector()
    private var currentSuccessUrl: String? = null

    private val _state = MutableStateFlow(AuthSocialScreenState())
    val state = _state.asStateFlow()

    private val _errorEvent = EventFlow<Unit>()
    val errorEvent = _errorEvent.observe()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            runCatching {
                authRepository.getSocialAuth(argExtra.key)
            }.onSuccess { data ->
                detector.loadUrl(data.socialUrl)
                _state.update { it.copy(data = data) }
            }.onFailure {
                authSocialAnalytics.error(it)
                errorHandler.handle(it)
            }
        }
    }

    fun onClearDataClick() {
        currentSuccessUrl = null
        detector.reset()
        detector.clearCookies()
        loadData()
        _state.update { it.copy(showClearCookies = false) }
    }

    fun onContinueClick() {
        _state.update { it.copy(showClearCookies = false) }
        currentSuccessUrl?.also { signSocial(it) }
    }

    fun submitUseTime(time: Long) {
        authSocialAnalytics.useTime(time)
    }

    fun onSuccessAuthResult(result: String) {
        if (detector.isSoFast()) {
            currentSuccessUrl = result
            _state.update { it.copy(showClearCookies = true) }
        } else {
            signSocial(result)
        }
    }

    fun onUserUnderstandWhatToDo() {
        router.exit()
    }

    fun sendAnalyticsPageError(error: Exception) {
        authSocialAnalytics.error(error)
    }

    fun onPageStateChanged(pageState: WebPageViewState) {
        _state.update { it.copy(pageState = pageState) }
    }

    private fun signSocial(resultUrl: String) {
        val data = _state.value.data ?: return

        viewModelScope.launch {
            _state.update { it.copy(isAuthProgress = true) }
            runCatching {
                authRepository.signInSocial(resultUrl, data)
            }.onSuccess {
                authSocialAnalytics.success()
                router.finishChain()
            }.onFailure {
                authSocialAnalytics.error(it)
                if (it is SocialAuthException) {
                    _errorEvent.set(Unit)
                } else {
                    errorHandler.handle(it)
                    router.exit()
                }
            }
            _state.update { it.copy(isAuthProgress = true) }
        }
    }

}