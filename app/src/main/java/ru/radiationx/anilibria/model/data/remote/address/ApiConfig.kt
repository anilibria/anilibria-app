package ru.radiationx.anilibria.model.data.remote.address

import android.util.Log
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import ru.radiationx.anilibria.di.providers.ApiOkHttpProvider
import ru.radiationx.anilibria.di.providers.MainOkHttpProvider
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.di.qualifier.MainClient
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.system.ClientWrapper
import javax.inject.Inject

class ApiConfig @Inject constructor(
) {

    private val addresses = mutableListOf<ApiAddress>()
    private var activeAddressTag: String = ""
    private val possibleIps = mutableListOf<String>()
    private val proxyPings = mutableMapOf<String, Float>()

    private val needConfigRelay = PublishRelay.create<Boolean>()
    var needConfig = true

    init {
        activeAddressTag = Api.DEFAULT_ADDRESS.tag
        setAddresses(listOf(Api.DEFAULT_ADDRESS))
    }

    fun observeNeedConfig(): Observable<Boolean> = needConfigRelay.hide()

    fun updateNeedConfig(state: Boolean) {
        needConfig = state
        needConfigRelay.accept(needConfig)
    }

    fun updateActiveAddress(address: ApiAddress) {
        activeAddressTag = address.tag
        //apiClientWrapper.set(apiOkHttpProvider.get().get())
    }

    fun setProxyPing(proxy: ApiProxy, ping: Float) {
        proxyPings[proxy.tag] = ping
        proxy.ping = ping
    }

    @Synchronized
    fun setAddresses(items: List<ApiAddress>) {
        addresses.clear()
        if (items.find { it.tag == Api.DEFAULT_ADDRESS.tag } == null) {
            addresses.add(Api.DEFAULT_ADDRESS)
        }
        addresses.addAll(items)

        possibleIps.clear()
        val ips = addresses.map { it.ips + it.proxies.map { it.ip } }.reduce { acc, list -> acc.plus(list) }.toSet().toList()
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

    val tag: String?
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