package ru.radiationx.anilibria.screen.auth.otp

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.models.OtpInfo
import ru.radiationx.data.api.auth.models.OtpNotCreatedException
import ru.radiationx.data.api.auth.models.OtpNotFoundException
import ru.radiationx.data.api.auth.models.OtpWrongUserException
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class AuthOtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    val otpInfoData = MutableStateFlow<OtpInfo?>(null)
    val state = MutableStateFlow(State())

    private var timerJob: Job? = null
    private var signInJob: Job? = null

    init {
        loadOtpInfo()
    }

    fun onCompleteClick() {
        updateState(progress = true, error = "")
        signIn()
    }

    fun onExpiredClick() {
        updateState(progress = true, error = "")
        loadOtpInfo()
    }

    fun onRepeatClick() {
        updateState(progress = true, error = "")
        loadOtpInfo()
    }

    private fun signIn() {
        val code = otpInfoData.value?.code ?: return
        signInJob?.cancel()
        signInJob = viewModelScope.launch {
            coRunCatching {
                authRepository.signInOtp(code)
            }.onSuccess {
                guidedRouter.finishGuidedChain()
            }.onFailure {
                handleError(it)
            }
        }
    }

    private fun loadOtpInfo() {
        signInJob?.cancel()
        signInJob = viewModelScope.launch {
            coRunCatching {
                authRepository.getOtpInfo()
            }.onSuccess {
                otpInfoData.value = it
                startTimer(it)
                updateState(ButtonState.COMPLETE, false)
            }.onFailure {
                handleError(it)
            }
        }
    }

    private fun handleError(error: Throwable) {
        Timber.e(error)
        val buttonState = when (error) {
            is OtpNotFoundException -> ButtonState.REPEAT
            is OtpNotCreatedException -> ButtonState.REPEAT
            is OtpWrongUserException -> ButtonState.REPEAT
            else -> ButtonState.REPEAT
        }
        updateState(buttonState, false, error.message.orEmpty())
    }

    private fun startTimer(otpInfo: OtpInfo) {
        timerJob?.cancel()
        val time = otpInfo.expiresAt.time - System.currentTimeMillis()
        if (time < 0) {
            setExpired()
            return
        }
        timerJob = viewModelScope.launch {
            delay(otpInfo.remainingTime)
            setExpired()
        }
    }

    private fun setExpired() {
        signInJob?.cancel()
        updateState(ButtonState.EXPIRED, false, "")
    }

    private fun updateState(
        buttonState: ButtonState = state.value.buttonState,
        progress: Boolean = state.value.progress,
        error: String = state.value.error,
    ) {
        state.value = State(buttonState, progress, error)
    }

    data class State(
        val buttonState: ButtonState = ButtonState.COMPLETE,
        val progress: Boolean = false,
        val error: String = "",
    )

    enum class ButtonState {
        COMPLETE,
        EXPIRED,
        REPEAT
    }
}