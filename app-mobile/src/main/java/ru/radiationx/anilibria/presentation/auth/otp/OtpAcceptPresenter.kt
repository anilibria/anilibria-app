package ru.radiationx.anilibria.presentation.auth.otp

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.AuthDeviceAnalytics
import ru.radiationx.data.entity.app.auth.OtpAcceptedException
import ru.radiationx.data.repository.AuthRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class OtpAcceptPresenter @Inject constructor(
    router: Router,
    private val authRepository: AuthRepository,
    private val errorHandler: IErrorHandler,
    private val authDeviceAnalytics: AuthDeviceAnalytics
) : BasePresenter<OtpAcceptView>(router) {

    private var success = false
    private var progress = false
    private var error: String? = null


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        updateState()
    }

    fun submitUseTime(time: Long) {
        authDeviceAnalytics.useTime(time)
    }

    fun onAcceptClick(code: String) {
        if (progress || success) {
            return
        }
        if (code.isBlank()) {
            error = "Поле обязательно к заполнению"
            updateState()
            return
        }

        presenterScope.launch {
            progress = true
            updateState()

            runCatching {
                authRepository.acceptOtp(code)
            }.onSuccess {

                onSuccess()
            }.onFailure {
                authDeviceAnalytics.error(it)
                if (it is OtpAcceptedException) {
                    onSuccess()
                    return@onFailure
                }
                success = false
                errorHandler.handle(it) { throwable, s ->
                    error = s.orEmpty()
                }
            }

            progress = false
            updateState()
        }
    }

    private fun updateState() {
        viewState.setState(success, progress, error)
    }

    private fun onSuccess() {
        authDeviceAnalytics.success()
        error = null
        success = true
        startCloseTimer()
    }

    private fun startCloseTimer() {
        presenterScope.launch {
            delay(1500)
            viewState.close()
        }
    }
}