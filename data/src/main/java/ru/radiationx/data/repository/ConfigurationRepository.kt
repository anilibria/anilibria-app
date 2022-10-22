package ru.radiationx.data.repository

import com.stealthcopter.networktools.ping.PingOptions
import com.stealthcopter.networktools.ping.PingResult
import com.stealthcopter.networktools.ping.PingTools
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeout
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.api.ConfigurationApi
import java.net.InetAddress
import javax.inject.Inject

class ConfigurationRepository @Inject constructor(
    private val configurationApi: ConfigurationApi,
    private val schedulers: SchedulersProvider
) {

    private val pingRelay = MutableStateFlow<Map<String, PingResult>?>(null)

    suspend fun checkAvailable(apiUrl: String): Boolean = configurationApi
        .checkAvailable(apiUrl)

    suspend fun checkApiAvailable(apiUrl: String): Boolean = configurationApi
        .checkApiAvailable(apiUrl)

    suspend fun getConfiguration(): List<ApiAddress> = configurationApi
        .getConfiguration()

    suspend fun getPingHost(host: String): PingResult {
        return withTimeout(15_000) {
            PingTools.doNativePing(InetAddress.getByName(host), PingOptions())
        }.also {
            val map = if (pingRelay.value != null) {
                pingRelay.value!!.toMutableMap()
            } else {
                mutableMapOf()
            }
            map[host] = it
            pingRelay.value = map
        }
    }
}