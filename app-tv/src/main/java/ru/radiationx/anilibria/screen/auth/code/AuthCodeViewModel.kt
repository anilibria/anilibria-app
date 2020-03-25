package ru.radiationx.anilibria.screen.auth.code

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.auth.OtpInfo
import ru.radiationx.data.repository.AuthRepository
import toothpick.InjectConstructor
import java.lang.Exception
import java.util.concurrent.TimeUnit

@InjectConstructor
class AuthCodeViewModel(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    val otpInfoData = MutableLiveData<OtpInfo>()
    val state = MutableLiveData<ButtonState>()

    private var complete = false
    private var timerDisposable = Disposables.disposed()
    private var signInDisposable = Disposables.disposed()

    override fun onCreate() {
        super.onCreate()
        state.value = ButtonState.Complete(true)
        loadOtpInfo()
    }

    fun onCompleteClick() {
        complete = true
        state.value = ButtonState.Complete(true)
        signIn()
    }

    fun onExpiredClick() {
        state.value = ButtonState.Expired(true)
        loadOtpInfo()
    }

    fun onRepeatClick() {
        if (complete) {
            state.value = ButtonState.Complete(true)
            signIn()
        } else {
            state.value = ButtonState.Complete(true)
            loadOtpInfo()
        }
    }

    private fun signIn() {
        signInDisposable.dispose()
        signInDisposable = authRepository
            .signInOtp(otpInfoData.value!!.code)
            .lifeSubscribe({
                guidedRouter.finishGuidedChain()
            }, {
                it.printStackTrace()
                state.value = ButtonState.Repeat(false)
            })
    }

    private fun loadOtpInfo() {
        complete = false
        timerDisposable.dispose()
        authRepository
            .getOtpInfo()
            .lifeSubscribe({
                otpInfoData.value = it
                state.value = ButtonState.Complete(false)
                startTimer(it)
            }, {
                it.printStackTrace()
                state.value = ButtonState.Repeat(false)
            })
    }

    private fun startTimer(otpInfo: OtpInfo) {
        timerDisposable.dispose()
        val time = otpInfo.expiresAt.time - System.currentTimeMillis()
        Log.e("lalala", "startTimer for ${time}")
        timerDisposable = Single
            .timer(time, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe({
                complete = false
                signInDisposable.dispose()
                state.value = ButtonState.Expired(false)
            }, {
                it.printStackTrace()
            })
    }

    sealed class ButtonState(val progress: Boolean) {
        class Complete(progress: Boolean) : ButtonState(progress)
        class Expired(progress: Boolean) : ButtonState(progress)
        class Repeat(progress: Boolean) : ButtonState(progress)
    }
}