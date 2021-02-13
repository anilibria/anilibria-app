package ru.radiationx.data.system

import android.util.Log
import io.reactivex.Single
import okhttp3.*
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.NetworkResponse
import ru.radiationx.data.datasource.remote.address.ApiConfig
import javax.inject.Inject


open class Client @Inject constructor(
    private val clientWrapper: ClientWrapper,
    private val appCookieJar: AppCookieJar,
    private val apiConfig: ApiConfig
) : IClient {

    companion object {
        const val METHOD_GET = "GET"
        const val METHOD_POST = "POST"
        const val METHOD_PUT = "PUT"
        const val METHOD_DELETE = "DELETE"

        const val HEADER_HOST_IP = "Remote-Address"
        const val USER_AGENT =
            "mobileApp Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.170 Safari/537.36 OPR/53.0.2907.68"
    }

    override fun get(url: String, args: Map<String, String>): Single<String> =
        getFull(url, args).map { it.body }

    override fun post(url: String, args: Map<String, String>): Single<String> =
        postFull(url, args).map { it.body }

    override fun put(url: String, args: Map<String, String>): Single<String> =
        putFull(url, args).map { it.body }

    override fun delete(url: String, args: Map<String, String>): Single<String> =
        deleteFull(url, args).map { it.body }

    override fun getFull(url: String, args: Map<String, String>): Single<NetworkResponse> =
        request(METHOD_GET, url, args)

    override fun postFull(url: String, args: Map<String, String>): Single<NetworkResponse> =
        request(METHOD_POST, url, args)

    override fun putFull(url: String, args: Map<String, String>): Single<NetworkResponse> =
        request(METHOD_PUT, url, args)

    override fun deleteFull(url: String, args: Map<String, String>): Single<NetworkResponse> =
        request(METHOD_DELETE, url, args)

    private fun request(
        method: String,
        url: String,
        args: Map<String, String>
    ): Single<NetworkResponse> = Single
        .fromCallable {
            val body = getRequestBody(method, args)
            val httpUrl = getHttpUrl(url, method, args)
            val request = Request.Builder()
                .url(httpUrl)
                .method(method, body)
                .build()

            clientWrapper.get().newCall(request)
        }
        .flatMap { CallExecuteSingle(it) }
        .doOnSuccess {
            if (!it.isSuccessful) {
                throw HttpException(it.code(), it.message(), it)
            }
        }
        .map {
            NetworkResponse(
                getHttpUrl(url, method, args).toString(),
                it.code(),
                it.message(),
                it.request().url().toString(),
                it.body()?.string().orEmpty(),
                it.headers(HEADER_HOST_IP)?.firstOrNull()
            )
        }

    private fun getRequestBody(
        method: String,
        args: Map<String, String>
    ): RequestBody? = when (method) {
        METHOD_POST -> {
            FormBody.Builder()
                .apply {
                    args.forEach {
                        add(it.key, it.value)
                    }
                }
                .build()
        }
        METHOD_PUT, METHOD_DELETE -> {
            RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), "")
        }
        METHOD_GET -> null
        else -> throw Exception("Unknown method: $method")
    }

    private fun getHttpUrl(url: String, method: String, args: Map<String, String>): HttpUrl {
        var httpUrl = HttpUrl.parse(url) ?: throw Exception("URL incorrect: '$url'")
        if (method == METHOD_GET) {
            httpUrl = httpUrl.newBuilder().let { builder ->
                args.forEach { builder.addQueryParameter(it.key, it.value) }
                builder.build()
            }
        }
        return httpUrl
    }
}
