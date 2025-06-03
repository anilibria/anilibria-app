package ru.radiationx.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class NetworkObserver @Inject constructor(
    private val context: Context
) {

    private val manager = requireNotNull(context.getSystemService<ConnectivityManager>())

    private val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            availableNetworks.update { it + network.toString() }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            availableNetworks.update { it - network.toString() }
        }
    }

    private val availableNetworks = MutableStateFlow<Set<String>>(emptySet())

    init {
        manager.registerNetworkCallback(request, callback)
    }

    fun getHash(): Int {
        return availableNetworks.value.hashCode()
    }

    fun isAvailable(): Boolean {
        return availableNetworks.value.isNotEmpty()
    }

    suspend fun awaitAvailable() {
        availableNetworks.first { it.isNotEmpty() }
    }
}