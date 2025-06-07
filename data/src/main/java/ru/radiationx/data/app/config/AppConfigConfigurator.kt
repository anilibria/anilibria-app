package ru.radiationx.data.app.config

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.radiationx.data.app.config.models.AppConfigAddress
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class AppConfigConfigurator @Inject constructor(
    private val appConfig: AppConfigImpl,
    private val repository: AppConfigRepository,
    private val storage: AppConfigStorage
) {

    companion object {
        private const val TAG = "AppConfigUpdater"
    }

    private val mutex = Mutex()

    suspend fun configure() {
        mutex.withLock {
            if (appConfig.needsConfigure()) {
                stepConfigure()
            } else {
                Timber.tag(TAG).d("already configured")
            }
        }
    }

    private suspend fun stepConfigure() {
        updateStep(Step.Initial)

        coroutineScope {
            if (appConfig.needsUpdateConfig()) {
                // todo api2 revert
                //stepUpdateConfig()
            }

            val address = stepFindAddress()
            if (address != null) {
                appConfig.setReady(address)
            } else {
                showWarning("Не удалось найти подходящий сервер")
                appConfig.setDefault()
            }
        }

        updateStep(Step.Finish)
    }

    private suspend fun stepUpdateConfig() {
        coRunCatching {
            updateStep(Step.ConfigUpdating)
            repository.updateConfig()
        }.onSuccess {
            updateStep(Step.ConfigSuccess)
        }.onFailure {
            updateStepError(Step.ConfigFailure, it)
        }
    }

    private suspend fun stepFindAddress(): AppConfigAddress? {
        return coRunCatching {
            updateStep(Step.AddressFinding)
            val addresses = storage.get().addresses
            repository.findFastest(addresses)
        }.onSuccess {
            updateStepData(Step.AddressSuccess, it.id)
        }.onFailure {
            updateStepError(Step.AddressFailure, it)
        }.getOrNull()
    }

    private fun showWarning(message: String) {
        Timber.tag(TAG).w(message)
    }

    private fun updateStep(step: Step) {
        Timber.tag(TAG).i("new step $step")
    }

    private fun updateStepError(step: Step, error: Throwable) {
        Timber.tag(TAG).e(error, "new step $step with error:")
    }

    private fun updateStepData(step: Step, data: Any?) {
        Timber.tag(TAG).i("new step $step with data: $data")
    }


    private enum class Step {
        Initial,

        ConfigUpdating,
        ConfigSuccess,
        ConfigFailure,

        AddressFinding,
        AddressSuccess,
        AddressFailure,

        Finish
    }
}