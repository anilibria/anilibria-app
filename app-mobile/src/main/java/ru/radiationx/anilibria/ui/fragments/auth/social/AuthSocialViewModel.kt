package ru.radiationx.anilibria.ui.fragments.auth.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
import ru.radiationx.data.apinext.models.SocialType
import ru.radiationx.data.entity.domain.auth.SocialAuthException
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

data class AuthSocialExtra(
    val type: SocialType,
) : QuillExtra

class AuthSocialViewModel @Inject constructor(
    private val argExtra: AuthSocialExtra,
    private val authRepository: AuthRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val authSocialAnalytics: AuthSocialAnalytics,
) : ViewModel() {

    private val detector = WebAuthSoFastDetector()
    private var currentSuccessUrl: String? = null

    private val _state = MutableStateFlow(AuthSocialScreenState())
    val state = _state.asStateFlow()

    private val _errorEvent = EventFlow<Unit>()
    val errorEvent = _errorEvent.observe()

    private val _reloadEvent = EventFlow<Unit>()
    val reloadEvent = _reloadEvent.observe()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            coRunCatching {
                authRepository.loadSocial(argExtra.type)
            }.onSuccess { data ->
                detector.loadUrl(data.url)
                _state.update { it.copy(data = data) }
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onClearDataClick() {
        viewModelScope.launch {
            currentSuccessUrl = null
            detector.reset()
            detector.clearCookies()
            detector.loadUrl(state.value.data?.url)
            _reloadEvent.set(Unit)
            _state.update { it.copy(showClearCookies = false) }
        }
    }

    fun onContinueClick() {
        _state.update { it.copy(showClearCookies = false) }
        currentSuccessUrl?.also { signSocial(it) }
    }

    fun submitUseTime(time: Long) {
        authSocialAnalytics.useTime(argExtra.type.key, time)
    }

    fun onSuccessAuthResult(resultUrl: String) {
        if (detector.isSoFast()) {
            currentSuccessUrl = resultUrl
            _state.update { it.copy(showClearCookies = true) }
        } else {
            signSocial(resultUrl)
        }
    }

    fun onUserUnderstandWhatToDo() {
        router.exit()
    }

    fun sendAnalyticsPageError(error: Exception) {
        Timber.e(error, "sendAnalyticsPageError")
        authSocialAnalytics.pageError(argExtra.type.key)
    }

    fun onPageStateChanged(pageState: WebPageViewState) {
        _state.update { it.copy(pageState = pageState) }
    }

    private fun signSocial(resultUrl: String) {
        val data = _state.value.data ?: return

        viewModelScope.launch {
            _state.update { it.copy(isAuthProgress = true) }
            coRunCatching {
                authRepository.signInSocial(resultUrl, data.state)
            }.onSuccess {
                authSocialAnalytics.success(argExtra.type.key)
                router.finishChain()
            }.onFailure {
                authSocialAnalytics.error(argExtra.type.key)
                if (it is SocialAuthException) {
                    _errorEvent.set(Unit)
                } else {
                    errorHandler.handle(it)
                    router.finishChain()
                }
            }
            _state.update { it.copy(isAuthProgress = true) }
        }
    }

}