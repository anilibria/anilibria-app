package ru.radiationx.anilibria.model.system

import okhttp3.OkHttpClient
import ru.radiationx.anilibria.model.data.remote.IClient

class ClientWrapper(private var client: OkHttpClient) {
    @Synchronized fun set(client: OkHttpClient) {
        this.client = client
    }

    @Synchronized fun get(): OkHttpClient = client
}