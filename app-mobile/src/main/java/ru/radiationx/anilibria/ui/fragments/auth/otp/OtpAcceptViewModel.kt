package ru.radiationx.anilibria.ui.fragments.auth.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.AuthDeviceAnalytics
import ru.radiationx.data.apinext.models.OtpCode
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import toothpick.InjectConstructor

@InjectConstructor
class OtpAcceptViewModel(
    private val authRepository: AuthRepository,
    private val errorHandler: IErrorHandler,
    private val authDeviceAnalytics: AuthDeviceAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow(OtpAcceptScreenState())
    val state = _state.asStateFlow()

    private val _closeEvent = EventFlow<Unit>()
    val closeEvent = _closeEvent.observe()

    fun submitUseTime(time: Long) {
        authDeviceAnalytics.useTime(time)
    }

    fun onAcceptClick(code: String) {
        if (_state.value.let { it.progress || it.success }) {
            return
        }
        if (code.isBlank()) {
            _state.update { it.copy(error = "Поле обязательно к заполнению") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(progress = true) }

            coRunCatching {
                authRepository.acceptOtp(OtpCode(code))
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                authDeviceAnalytics.error(error)
                _state.update { it.copy(success = false) }
                errorHandler.handle(error) { _, s ->
                    _state.update { it.copy(error = s.orEmpty()) }
                }
            }

            _state.update { it.copy(progress = false) }
        }
    }

    private fun onSuccess() {
        authDeviceAnalytics.success()
        _state.update { it.copy(success = true, error = null) }
        startCloseTimer()
    }

    private fun startCloseTimer() {
        viewModelScope.launch {
            delay(1500)
            _closeEvent.set(Unit)
        }
    }
}

data class OtpAcceptScreenState(
    val success: Boolean = false,
    val progress: Boolean = false,
    val error: String? = null
)