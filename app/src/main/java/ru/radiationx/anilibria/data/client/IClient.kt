package ru.radiationx.anilibria.data.client

import io.reactivex.Single

interface IClient {


    fun get(url: String, args: Map<String, String>): Single<String>

    fun post(url: String, args: Map<String, String>): Single<String>

    fun put(url: String, args: Map<String, String>): Single<String>

    fun delete(url: String, args: Map<String, String>): Single<String>
}
