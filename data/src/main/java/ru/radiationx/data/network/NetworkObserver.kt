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
import timber.log.Timber
import javax.inject.Inject

class NetworkObserver @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "NetworkObserver"
    }

    private val manager = requireNotNull(context.getSystemService<ConnectivityManager>())

    private val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        .build()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            availableNetworks.update { it + network.toModel() }
            logNetworks()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            availableNetworks.update { it - network.toModel() }
            logNetworks()
        }

        private fun logNetworks() {
            Timber.tag(TAG).d("networks=${availableNetworks.value}; isAvailable=${isAvailable()}")
        }
    }

    private val availableNetworks = MutableStateFlow<Set<NetworkModel>>(emptySet())

    init {
        manager.registerNetworkCallback(request, callback)
    }

    fun getHash(): Int {
        return availableNetworks.value.hashCode()
    }

    fun isAvailable(): Boolean {
        return availableNetworks.value.isAvailable()
    }

    suspend fun awaitAvailable() {
        availableNetworks.first { it.isAvailable() }
    }

    private fun Set<NetworkModel>.isAvailable(): Boolean {
        return count { !it.isVpn } > 0
    }

    @Suppress("DEPRECATION")
    private fun Network.toModel(): NetworkModel {
        val info = manager.getNetworkInfo(this)
        val isVpn = info?.type == ConnectivityManager.TYPE_VPN
        return NetworkModel(
            network = this,
            isVpn = isVpn
        )
    }

    private data class NetworkModel(
        val network: Network,
        val isVpn: Boolean
    )
}