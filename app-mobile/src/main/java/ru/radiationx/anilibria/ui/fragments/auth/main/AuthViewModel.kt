package ru.radiationx.anilibria.ui.fragments.auth.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.SocialAuthItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.AuthMainAnalytics
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
import ru.radiationx.data.apinext.models.Credentials
import ru.radiationx.data.apinext.models.SocialType
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

@InjectConstructor
class AuthViewModel(
    private val router: Router,
    private val systemMessenger: SystemMessenger,
    private val authRepository: AuthRepository,
    private val errorHandler: IErrorHandler,
    private val authMainAnalytics: AuthMainAnalytics,
    private val authSocialAnalytics: AuthSocialAnalytics,
    private val apiConfig: ApiConfig,
    private val systemUtils: SystemUtils,
) : ViewModel() {

    private val inputDataState = MutableStateFlow(AuthInputData())

    private val socialState = SocialType.entries.map { it.toState() }
    private val _state = MutableStateFlow(AuthScreenState(socialItems = socialState))
    val state = _state.asStateFlow()

    private val _registrationEvent = EventFlow<Unit>()
    val registrationEvent = _registrationEvent.observe()

    init {
        inputDataState
            .map { it.login.isNotEmpty() && it.password.isNotEmpty() }
            .onEach { enabled ->
                _state.update { it.copy(actionEnabled = enabled) }
            }
            .launchIn(viewModelScope)
    }

    fun onSocialClick(item: SocialAuthItemState) {
        authMainAnalytics.socialClick(item.type.key)
        authSocialAnalytics.open(AnalyticsConstants.screen_auth_main)
        router.navigateTo(Screens.AuthSocial(item.type))
    }

    fun setLogin(login: String) {
        inputDataState.update { it.copy(login = login) }
    }

    fun setPassword(password: String) {
        inputDataState.update { it.copy(password = password) }
    }

    fun signIn() {
        authMainAnalytics.loginClick()
        viewModelScope.launch {
            _state.update { it.copy(sending = true) }
            val inputData = inputDataState.value
            coRunCatching {
                authRepository.signIn(Credentials(inputData.login, inputData.password))
                authRepository.getAuthState()
            }.onSuccess {
                decideWhatToDo(it)
            }.onFailure {
                authMainAnalytics.error(it)
                errorHandler.handle(it)
            }
            _state.update { it.copy(sending = false) }
        }
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

    fun onBackPressed() {
        viewModelScope.launch {
            val state = authRepository.getAuthState()
            if (state != AuthState.AUTH) {
                skip()
            } else {
                router.finishChain()
            }
        }
    }

    fun skip() {
        viewModelScope.launch {
            authMainAnalytics.skipClick()
            authRepository.setAuthSkipped(true)
            router.finishChain()
        }
    }

    fun registrationClick() {
        authMainAnalytics.regClick()
        _registrationEvent.set(Unit)
    }

    fun registrationToSiteClick() {
        authMainAnalytics.regToSiteClick()
        systemUtils.externalLink("${apiConfig.siteUrl}/pages/login.php")
    }

    fun submitUseTime(time: Long) {
        authMainAnalytics.useTime(time)
    }

}

data class AuthInputData(
    val login: String = "",
    val password: String = "",
)

data class AuthScreenState(
    val actionEnabled: Boolean = false,
    val sending: Boolean = false,
    val socialItems: List<SocialAuthItemState> = emptyList(),
)
