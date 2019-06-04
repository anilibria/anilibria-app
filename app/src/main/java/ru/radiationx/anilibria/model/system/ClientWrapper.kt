package ru.radiationx.anilibria.model.system

import okhttp3.OkHttpClient
import ru.radiationx.anilibria.di.providers.ApiOkHttpProvider
import ru.radiationx.anilibria.model.data.remote.address.ApiConfigChanger
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