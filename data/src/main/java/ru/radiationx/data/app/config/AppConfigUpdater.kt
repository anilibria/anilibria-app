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
        private const val TAG = "ConfigInteractor"
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
        updateState(State.Initial)

        stepAwaitNetwork()

        if (appConfig.getNetworkHash() == null) {
            stepUpdateConfig()
        }

        val address = stepFindAddress()
        if (address != null) {
            appConfig.setReady(address)
        } else {
            appConfig.setDefault()
        }

        appConfig.setNetworkHash(networkObserver.getHash())

        updateState(State.Configured)
    }

    private suspend fun stepAwaitNetwork() {
        if (!networkObserver.isAvailable()) {
            showWarning("Ожидание подключения к интернету")
        }
        updateState(State.NetworkWaiting)
        networkObserver.awaitAvailable()
        updateState(State.NetworkAvailable)
    }

    private suspend fun stepUpdateConfig() {
        coRunCatching {
            updateState(State.ConfigUpdating)
            repository.updateConfig()
        }.onSuccess {
            updateState(State.ConfigSuccess)
        }.onFailure {
            updateStateError(State.ConfigFailure, it)
        }
    }

    private suspend fun stepFindAddress(): AppConfigAddress? {
        return coRunCatching {
            updateState(State.AddressFinding)
            val addresses = storage.get().addresses
            repository.findFastest(addresses)
        }.onSuccess {
            updateStateData(State.AddressSuccess, it.id)
        }.onFailure {
            updateStateError(State.AddressFailure, it)
        }.getOrNull()
    }

    private fun showWarning(message: String) {
        Timber.tag(TAG).w(message)
    }

    private fun updateState(state: State) {
        Timber.tag(TAG).i("new state $state")
    }

    private fun updateStateError(state: State, error: Throwable) {
        Timber.tag(TAG).e(error, "new state $state with error:")
    }

    private fun updateStateData(state: State, data: Any?) {
        Timber.tag(TAG).i("new state $state with data: $data")
    }


    private enum class State {
        Initial,

        NetworkWaiting,
        NetworkAvailable,

        ConfigUpdating,
        ConfigSuccess,
        ConfigFailure,

        AddressFinding,
        AddressSuccess,
        AddressFailure,

        Configured
    }
}