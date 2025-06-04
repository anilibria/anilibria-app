package ru.radiationx.shared_app.networkstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.data.app.config.AppConfig
import ru.radiationx.data.network.NetworkObserver
import javax.inject.Inject

class NetworkStatusViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val networkObserver: NetworkObserver
) : ViewModel() {

    private val _state = MutableStateFlow(NetworkStatusState.Connected)
    val state = _state.asStateFlow()

    init {
        networkObserver
            .observeAvailable()
            .flatMapLatest { hasNetwork ->
                if (!hasNetwork) {
                    flowOf(NetworkStatusState.NotConnected)
                } else if (appConfig.isConfigured) {
                    flowOf(NetworkStatusState.Connected)
                } else {
                    appConfig.configState.map { hasConfig ->
                        if (hasConfig) {
                            NetworkStatusState.Configured
                        } else {
                            NetworkStatusState.Configuring
                        }
                    }
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }
}

