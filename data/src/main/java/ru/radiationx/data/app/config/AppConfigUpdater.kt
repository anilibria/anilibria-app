package ru.radiationx.data.app.config

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.radiationx.data.app.config.models.AppConfigAddress
import ru.radiationx.data.network.NetworkObserver
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class AppConfigUpdater @Inject constructor(
    private val appConfig: AppConfigImpl,
    private val repository: AppConfigRepository,
    private val storage: AppConfigStorage,
    private val networkObserver: NetworkObserver
) {

    companion object {
        private const val TAG = "AppConfigUpdater"
    }

    private val mutex = Mutex()

    suspend fun update() {
        mutex.withLock {
            if (appConfig.needsUpdateAddress(networkObserver.getHash())) {
                stepConfigure()
            } else {
                Timber.tag(TAG).d("already configured")
            }
        }
    }

    private suspend fun stepConfigure() {
        updateState(Step.Initial)

        if (appConfig.getNetworkHash() == null) {
            stepUpdateConfig()
        }

        val address = stepFindAddress()
        if (address != null) {
            appConfig.setReady(address)
        } else {
            showWarning("Не удалось найти подходящий сервер")
            appConfig.setDefault()
        }

        appConfig.setNetworkHash(networkObserver.getHash())

        updateState(Step.Finish)
    }

    private suspend fun stepUpdateConfig() {
        coRunCatching {
            updateState(Step.ConfigUpdating)
            repository.updateConfig()
        }.onSuccess {
            updateState(Step.ConfigSuccess)
        }.onFailure {
            updateStateError(Step.ConfigFailure, it)
        }
    }

    private suspend fun stepFindAddress(): AppConfigAddress? {
        return coRunCatching {
            updateState(Step.AddressFinding)
            val addresses = storage.get().addresses
            repository.findFastest(addresses)
        }.onSuccess {
            updateStateData(Step.AddressSuccess, it.id)
        }.onFailure {
            updateStateError(Step.AddressFailure, it)
        }.getOrNull()
    }

    private fun showWarning(message: String) {
        Timber.tag(TAG).w(message)
    }

    private fun updateState(step: Step) {
        Timber.tag(TAG).i("new state $step")
    }

    private fun updateStateError(step: Step, error: Throwable) {
        Timber.tag(TAG).e(error, "new state $step with error:")
    }

    private fun updateStateData(step: Step, data: Any?) {
        Timber.tag(TAG).i("new state $step with data: $data")
    }


    private enum class Step {
        Initial,

        ConfigUpdating,
        ConfigSuccess,
        ConfigFailure,

        AddressFinding,
        AddressSuccess,
        AddressFailure,

        RepeatAddress,

        Finish
    }
}