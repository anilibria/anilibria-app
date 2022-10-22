package ru.radiationx.anilibria.screen.auth.otp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.auth.OtpInfo
import ru.radiationx.data.entity.app.auth.OtpNotAcceptedException
import ru.radiationx.data.entity.app.auth.OtpNotFoundException
import ru.radiationx.data.repository.AuthRepository
import toothpick.InjectConstructor

@InjectConstructor
class AuthOtpViewModel(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    val otpInfoData = MutableLiveData<OtpInfo>()
    val state = MutableLiveData<State>()

    private var timerJob: Job? = null
    private var signInJob: Job? = null

    init {
        state.value = State()
    }

    override fun onCreate() {
        super.onCreate()
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
        signInJob?.cancel()
        signInJob = viewModelScope.launch {
            runCatching {
                authRepository.signInOtp(otpInfoData.value!!.code)
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
            runCatching {
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
        error.printStackTrace()
        val buttonState = when (error) {
            is OtpNotFoundException -> ButtonState.EXPIRED
            is OtpNotAcceptedException -> ButtonState.COMPLETE
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
        buttonState: ButtonState = state.value!!.buttonState,
        progress: Boolean = state.value!!.progress,
        error: String = state.value!!.error
    ) {
        state.value = State(buttonState, progress, error)
    }

    data class State(
        val buttonState: ButtonState = ButtonState.COMPLETE,
        val progress: Boolean = false,
        val error: String = ""
    )

    enum class ButtonState {
        COMPLETE,
        EXPIRED,
        REPEAT
    }
}