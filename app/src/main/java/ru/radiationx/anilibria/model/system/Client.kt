package ru.radiationx.anilibria.model.system

import android.content.Context
import android.util.Log
import io.reactivex.Single
import okhttp3.*
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.holders.UserHolder
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.NetworkResponse
import java.io.IOException
import javax.inject.Inject


class Client @Inject constructor(
        private val cookieHolder: CookieHolder,
        private val userHolder: UserHolder,
        private val context: Context
) : IClient {

    companion object {
        const val METHOD_GET = "GET"
        const val METHOD_POST = "POST"
        const val METHOD_PUT = "PUT"
        const val METHOD_DELETE = "DELETE"
    }

    private val cookieJar = object : CookieJar {

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            Log.e("IClient", "saveFromResponse ${cookies.joinToString { "${it.name()}=${it.value()}" }}")
            var authDestroyed = false
            for (cookie in cookies) {
                if (cookie.value() == "deleted") {
                    if (cookie.name() == CookieHolder.PHPSESSID) {
                        authDestroyed = true
                    }
                    cookieHolder.removeCookie(cookie.name())
                } else {
                    cookieHolder.putCookie(url.toString(), cookie)
                }
            }
            if (authDestroyed) {
                userHolder.delete()
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieHolder.getCookies().values.map { it }
        }
    }

    private val client = OkHttpClient.Builder()
            /*.addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })*/
            .addInterceptor {
                val userAgentRequest = it.request()
                        .newBuilder()
                        .header("mobileApp", "true")
                        .header("User-Agent", "mobileApp Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.170 Safari/537.36 OPR/53.0.2907.68")
                        .build()
                it.proceed(userAgentRequest)
            }
            .cookieJar(cookieJar)
            .build()

    override fun get(url: String, args: Map<String, String>): Single<String> = getFull(url, args)
            .map { it.body }

    override fun post(url: String, args: Map<String, String>): Single<String> = postFull(url, args)
            .map { it.body }

    override fun put(url: String, args: Map<String, String>): Single<String> = putFull(url, args)
            .map { it.body }

    override fun delete(url: String, args: Map<String, String>): Single<String> = deleteFull(url, args)
            .map { it.body }

    override fun getFull(url: String, args: Map<String, String>): Single<NetworkResponse> = Single
            .fromCallable { request(METHOD_GET, url, args) }

    override fun postFull(url: String, args: Map<String, String>): Single<NetworkResponse> = Single
            .fromCallable { request(METHOD_POST, url, args) }

    override fun putFull(url: String, args: Map<String, String>): Single<NetworkResponse> = Single
            .fromCallable { request(METHOD_PUT, url, args) }

    override fun deleteFull(url: String, args: Map<String, String>): Single<NetworkResponse> = Single
            .fromCallable { request(METHOD_DELETE, url, args) }

    private fun getRequestBody(method: String, args: Map<String, String>): RequestBody? {
        return when (method) {
            METHOD_POST -> {
                val requestBody = FormBody.Builder()
                //.setType(MultipartBody.FORM)

                args.forEach {
                    Log.e("S_DEF_LOG", "ADD PART " + it.key + " : " + it.value)
                    requestBody.add(it.key, it.value)
                }

                val suk = requestBody.build()
                Log.e("S_DEF_LOG", "CONT TYPE ${suk.contentType()}")
                suk
            }
            METHOD_PUT, METHOD_DELETE -> {
                RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), "")
            }
            METHOD_GET -> null
            else -> throw Exception("Unknown method: $method")
        }
    }

    private fun simpleRequest(method: String, url: String, args: Map<String, String>): NetworkResponse {

        val body = getRequestBody(method, args)

        var httpUrl: HttpUrl = HttpUrl.parse(url) ?: throw Exception("URL incorrect: '$url'")

        if (method == METHOD_GET) {
            httpUrl = httpUrl.newBuilder().let { builder ->
                args.forEach { builder.addQueryParameter(it.key, it.value) }
                builder.build()
            }
        }

        val request = Request.Builder()
                .url(httpUrl)
                .method(method, body)
                .build()

        val response = NetworkResponse(httpUrl.toString())

        Log.e("S_DEF_LOG", "REQUEST $httpUrl : $method : $body : args={${args.toList().joinToString { "${it.first}: ${it.second}" }}}")

        var okHttpResponse: Response? = null
        var responseBody: ResponseBody? = null
        try {
            okHttpResponse = client.newCall(request).execute()
            if (!okHttpResponse!!.isSuccessful)
                throw IOException("Unexpected code $okHttpResponse")
            responseBody = okHttpResponse.body()

            response.code = okHttpResponse.code()
            response.message = okHttpResponse.message()
            response.redirect = okHttpResponse.request().url().toString()
            response.body = responseBody?.string().orEmpty()
        } finally {
            okHttpResponse?.close()
            responseBody?.close()
        }
        return response
    }

    @Throws(Exception::class)
    private fun request(method: String, url: String, args: Map<String, String>): NetworkResponse =
            simpleRequest(method, url, args)

}
