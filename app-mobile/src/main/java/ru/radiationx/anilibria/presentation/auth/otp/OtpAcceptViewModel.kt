package ru.radiationx.anilibria.presentation.auth.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.AuthDeviceAnalytics
import ru.radiationx.data.entity.domain.auth.OtpAcceptedException
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.shared.ktx.EventFlow
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

            runCatching {
                authRepository.acceptOtp(code)
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                authDeviceAnalytics.error(error)
                if (error is OtpAcceptedException) {
                    onSuccess()
                    return@onFailure
                }
                _state.update { it.copy(success = false) }
                errorHandler.handle(error) { _, s ->
                    _state.update { it.copy(error = s.orEmpty()) }
                }
            }

            _state.update { it.copy(progress = true) }
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