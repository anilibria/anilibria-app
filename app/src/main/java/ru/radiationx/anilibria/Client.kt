package ru.radiationx.anilibria

import android.util.Log
import io.reactivex.Single
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.anilibria.model.data.holders.AuthHolder
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.holders.UserHolder
import ru.radiationx.anilibria.model.data.remote.IClient
import java.io.IOException


class Client constructor(
        private val cookieHolder: CookieHolder,
        private val userHolder: UserHolder
) : IClient {
    companion object {
        const val METHOD_GET = "GET"
        const val METHOD_POST = "POST"
        const val METHOD_PUT = "PUT"
        const val METHOD_DELETE = "DELETE"
    }

    private val cookieJar = object : CookieJar {

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            var authDestroyed = false
            for (cookie in cookies) {
                if (cookie.value() == "deleted") {
                    if (cookie.name() == CookieHolder.BITRIX_SM_UIDH || cookie.name() == CookieHolder.BITRIX_SM_UIDL) {
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
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .cookieJar(cookieJar)
            .build()

    override fun get(url: String, args: Map<String, String>): Single<String> = Single.fromCallable {
        request(METHOD_GET, url, args)
    }

    override fun post(url: String, args: Map<String, String>): Single<String> = Single.fromCallable {
        request(METHOD_POST, url, args)
    }

    override fun put(url: String, args: Map<String, String>): Single<String> = Single.fromCallable {
        request(METHOD_PUT, url, args)
    }

    override fun delete(url: String, args: Map<String, String>): Single<String> = Single.fromCallable {
        request(METHOD_DELETE, url, args)
    }

    private fun getRequestBody(method: String, args: Map<String, String>): RequestBody? {
        return when (method) {
            METHOD_POST -> {
                val requestBody = FormBody.Builder()
                //.setType(MultipartBody.FORM)

                args.forEach {
                    Log.e("SUKA", "ADD PART " + it.key + " : " + it.value)
                    requestBody.add(it.key, it.value)
                }

                val suk = requestBody.build()
                Log.e("SUKA", "CONT TYPE ${suk.contentType()}")
                suk
            }
            METHOD_PUT, METHOD_DELETE -> {
                RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), "")
            }
            METHOD_GET -> null
            else -> throw NullPointerException("Unknown method: $method")
        }
    }

    @Throws(Exception::class)
    private fun request(method: String, url: String, args: Map<String, String>): String {
        val body = getRequestBody(method, args)

        var httpUrl: HttpUrl = HttpUrl.parse(url) ?: throw Exception("URL incorrect")

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

        Log.e("SUKA", "REQUEST $httpUrl : $method : $body")

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
