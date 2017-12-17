package ru.radiationx.anilibria.data.client

import io.reactivex.Single
import okhttp3.*
import java.io.IOException


class Client : IClient {

    private val client = OkHttpClient.Builder()
            /*.addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })*/
            .build()

    override fun get(url: String, args: Map<String, String>): Single<String> = Single.fromCallable {
        request("GET", url, args)
    }

    override fun post(url: String, args: Map<String, String>): Single<String> = Single.fromCallable {
        request("POST", url, args)
    }

    override fun put(url: String, args: Map<String, String>): Single<String> = Single.fromCallable {
        request("PUT", url, args)
    }

    override fun delete(url: String, args: Map<String, String>): Single<String> = Single.fromCallable {
        request("DELETE", url, args)
    }

    @Throws(Exception::class)
    private fun request(method: String, url: String, args: Map<String, String>): String {
        val body = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), "")

        val httpUrl: HttpUrl = HttpUrl.parse(url)?.newBuilder(url)?.let { builder ->
            args.forEach {
                builder.addQueryParameter(it.key, it.value)
            }
            builder.build()
        }!!

        val request = Request.Builder()
                .url(httpUrl)
                .get()
                .method(method, if (method == "GET") null else body)
                .build()

        var okHttpResponse: Response? = null
        var responseBody: ResponseBody? = null
        try {
            okHttpResponse = client.newCall(request).execute()
            if (!okHttpResponse!!.isSuccessful)
                throw IOException("Unexpected code " + okHttpResponse)

            responseBody = okHttpResponse.body()
            return responseBody?.string().orEmpty()
        } finally {
            okHttpResponse?.close()
            responseBody?.close()
        }
    }
}
