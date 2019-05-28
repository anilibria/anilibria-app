package ru.radiationx.anilibria.model.data.remote.address

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import ru.radiationx.anilibria.model.data.remote.Api
import javax.inject.Inject

class ApiConfig @Inject constructor() {

    private val addresses = mutableListOf<ApiAddress>(Api.DEFAULT_ADDRESS)
    private var activeAddressTag: String = Api.DEFAULT_ADDRESS.tag

    private val needConfigRelay = PublishRelay.create<Boolean>()
    var needConfig = true

    fun observeNeedConfig(): Observable<Boolean> = needConfigRelay.hide()

    fun updateNeedConfig(state: Boolean) {
        needConfig = state
        needConfigRelay.accept(needConfig)
    }

    fun updateActiveAddress(address: ApiAddress){
        activeAddressTag = address.tag
    }

    @Synchronized
    fun setAddresses(items: List<ApiAddress>) {
        addresses.clear()
        if (items.find { it.tag == Api.DEFAULT_ADDRESS.tag } == null) {
            addresses.add(Api.DEFAULT_ADDRESS)
        }
        addresses.addAll(items)
    }

    @Synchronized
    fun getAddresses(): List<ApiAddress> = addresses.toList()

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