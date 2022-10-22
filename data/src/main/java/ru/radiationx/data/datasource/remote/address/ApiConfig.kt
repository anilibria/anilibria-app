package ru.radiationx.data.datasource.remote.address

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.radiationx.data.datasource.remote.Api
import ru.radiationx.data.datasource.storage.ApiConfigStorage
import javax.inject.Inject

class ApiConfig @Inject constructor(
    private val configChanger: ApiConfigChanger,
    private val apiConfigStorage: ApiConfigStorage
) {

    private val addresses = mutableListOf<ApiAddress>()
    private var activeAddressTag: String = ""
    private val possibleIps = mutableListOf<String>()
    private val proxyPings = mutableMapOf<String, Float>()

    private val needConfigRelay = MutableSharedFlow<Boolean>()
    var needConfig = true

    init {
        activeAddressTag = apiConfigStorage.getActive() ?: Api.DEFAULT_ADDRESS.tag
        val initAddresses = apiConfigStorage.get() ?: listOf(Api.DEFAULT_ADDRESS)
        setAddresses(initAddresses)
    }

    fun observeNeedConfig(): Flow<Boolean> = needConfigRelay

    suspend fun updateNeedConfig(state: Boolean) {
        needConfig = state
        needConfigRelay.emit(needConfig)
    }

    suspend fun updateActiveAddress(address: ApiAddress) {
        activeAddressTag = address.tag
        apiConfigStorage.setActive(activeAddressTag)
        configChanger.onChange()
    }

    fun setProxyPing(proxy: ApiProxy, ping: Float) {
        proxyPings[proxy.tag] = ping
        proxy.ping = ping
    }

    @Synchronized
    fun setAddresses(items: List<ApiAddress>) {
        addresses.clear()
        /*if (items.find { it.tag == Api.DEFAULT_ADDRESS.tag } == null) {
            addresses.add(Api.DEFAULT_ADDRESS)
        }*/
        addresses.addAll(items)

        possibleIps.clear()
        val ips = addresses.map { it.ips + it.proxies.map { it.ip } }
            .reduce { acc, list -> acc.plus(list) }.toSet().toList()
        possibleIps.addAll(ips)

        addresses.forEach {
            it.proxies.forEach { proxy ->
                proxyPings[it.tag]?.also {
                    proxy.ping = it
                }
            }
        }
    }

    @Synchronized
    fun getAddresses(): List<ApiAddress> = addresses.toList()

    @Synchronized
    fun getPossibleIps(): List<String> = possibleIps.toList()

    val active: ApiAddress
        get() = addresses.firstOrNull { it.tag == activeAddressTag } ?: Api.DEFAULT_ADDRESS

    val tag: String
        get() = active.tag

    val name: String?
        get() = active.name

    val desc: String?
        get() = active.desc

    val widgetsSiteUrl: String
        get() = active.widgetsSite

    val siteUrl: String
        get() = active.site

    val baseImagesUrl: String
        get() = active.baseImages

    val baseUrl: String
        get() = active.base

    val apiUrl: String
        get() = active.api

    val ips: List<String>
        get() = active.ips

    val proxies: List<ApiProxy>
        get() = active.proxies
}