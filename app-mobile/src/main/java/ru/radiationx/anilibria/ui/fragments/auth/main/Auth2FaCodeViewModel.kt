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
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.features.AuthMainAnalytics
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.auth.WrongPasswordException
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

data class Auth2FaCodeExtra(
    val login: String,
    val password: String
) : QuillExtra

class Auth2FaCodeViewModel @Inject constructor(
    private val argExtra: Auth2FaCodeExtra,
    private val router: Router,
    private val systemMessenger: SystemMessenger,
    private val authRepository: AuthRepository,
    private val errorHandler: IErrorHandler,
    private val authMainAnalytics: AuthMainAnalytics
) : ViewModel() {

    private val codeState = MutableStateFlow("")

    private val _state = MutableStateFlow(Auth2FaCodeScreenState())
    val state = _state.asStateFlow()

    init {
        codeState.map { it.isNotEmpty() }.onEach { enabled ->
            _state.update { it.copy(actionEnabled = enabled) }
        }.launchIn(viewModelScope)
    }

    fun setCode2fa(code2fa: String) {
        codeState.value = code2fa
    }

    fun signIn() {
        viewModelScope.launch {
            _state.update { it.copy(sending = true) }
            coRunCatching {
                authRepository.signIn(argExtra.login, argExtra.password, codeState.value)
                authRepository.getAuthState()
            }.onSuccess {
                decideWhatToDo(it)
            }.onFailure {
                authMainAnalytics.error(it)
                errorHandler.handle(it)
                if (it is WrongPasswordException) {
                    router.exit()
                }
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
}

data class Auth2FaCodeScreenState(
    val actionEnabled: Boolean = false,
    val sending: Boolean = false
)
