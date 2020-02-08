package ru.radiationx.anilibria.model.system

import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider

open class ClientWrapper @Inject constructor(
        private val provider: Provider<OkHttpClient>
) {

    private var client: OkHttpClient = provider.get()

    @Synchronized
    fun set(client: OkHttpClient) {
        this.client = client
    }

    @Synchronized
    fun get(): OkHttpClient = client

}