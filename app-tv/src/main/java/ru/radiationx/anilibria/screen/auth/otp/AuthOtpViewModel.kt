package ru.radiationx.anilibria.screen.auth.otp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.auth.OtpInfo
import ru.radiationx.data.entity.app.auth.OtpNotAcceptedException
import ru.radiationx.data.entity.app.auth.OtpNotFoundException
import ru.radiationx.data.repository.AuthRepository
import toothpick.InjectConstructor
import java.util.*
import java.util.concurrent.TimeUnit

@InjectConstructor
class AuthOtpViewModel(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    val otpInfoData = MutableLiveData<OtpInfo>()
    val state = MutableLiveData<State>()

    private var timerDisposable = Disposables.disposed()
    private var signInDisposable = Disposables.disposed()

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
        signInDisposable.dispose()
        signInDisposable = authRepository
            .signInOtp(otpInfoData.value!!.code)
            .lifeSubscribe({
                guidedRouter.finishGuidedChain()
            }, {
                handleError(it)
            })
    }

    private fun loadOtpInfo() {
        signInDisposable.dispose()
        authRepository
            .getOtpInfo()
            .lifeSubscribe({
                otpInfoData.value = it
                startTimer(it)
                updateState(ButtonState.COMPLETE, false)
            }, {
                handleError(it)
            })
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
        timerDisposable.dispose()
        val time = otpInfo.expiresAt.time - System.currentTimeMillis()
        if (time < 0) {
            setExpired()
            return
        }
        timerDisposable = Single
            .timer(otpInfo.remainingTime, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe({
                setExpired()
            }, {
                it.printStackTrace()
            })
    }

    private fun setExpired() {
        signInDisposable.dispose()
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