package ru.radiationx.anilibria

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.reactivex.Single
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.holders.UserHolder
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.ui.fragments.BlazingFastActivity
import ru.radiationx.anilibria.ui.fragments.GoogleCaptchaActivity
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class Client constructor(
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

    private val blazingFastPattern = Pattern.compile("open[^\\(]*?\\([^\"']*?[\"'][^\"']+[\"'][^,]*?,[^\"']*?[\"']([\\s\\S]*?)[\"']\\s*?(?:,[\\s]*?(?:true|false)|\\))")
    private val jsConcatStringPattern = Pattern.compile("[\"'][\\s]*?\\+[\\s]*?[^\\+]*?[\\s]*?\\+[\\s]*?[\"']")
    private val googleCaptchaPattern = Pattern.compile("g-recaptcha\" data-sitekey")

    private val recurseControl = mutableMapOf<String, Int>()

    private val cookieJar = object : CookieJar {

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            Log.e("IClient", "saveFromResponse ${cookies.joinToString { it.name() }}")
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
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor {
                val userAgentRequest = it.request()
                        .newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.170 Safari/537.36 OPR/53.0.2907.68")
                        .build()
                it.proceed(userAgentRequest)
            }
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

        Log.e("S_DEF_LOG", "REQUEST $httpUrl : $method : $body")

        var okHttpResponse: Response? = null
        var responseBody: ResponseBody? = null
        var stringBody: String = ""
        var redirectUrl = url
        try {
            okHttpResponse = client.newCall(request).execute()
            if (!okHttpResponse!!.isSuccessful)
                throw IOException("Unexpected code $okHttpResponse")

            redirectUrl = okHttpResponse.request().url().toString()
            responseBody = okHttpResponse.body()
            stringBody = responseBody?.string().orEmpty()
        } finally {
            okHttpResponse?.close()
            responseBody?.close()
        }

        googleCaptchaResolver(stringBody, redirectUrl)
        val bfr = blazingFastResolver(stringBody)
        /*if (bfr) {
            val recurseLevel = recurseControl[httpUrl.toString()] ?: 0
            if (recurseLevel >= 1) {
                return stringBody
            }
            recurseControl[httpUrl.toString()] = recurseLevel + 1
            stringBody = request(method, url, args)
            if (recurseLevel <= 1) {
                recurseControl.remove(httpUrl.toString())
            }
        }*/
        return stringBody;
    }

    private fun googleCaptchaResolver(response: String, url: String) {
        if (!googleCaptchaPattern.matcher(response).find()/* && !blazingFastPattern.matcher(response).find()*/) {
            return
        }
        Handler(Looper.getMainLooper()).post {
            try {
                context.startActivity(Intent(context, GoogleCaptchaActivity::class.java).apply {
                    putExtra("content", response)
                    putExtra("url", url)
                }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        throw Exception("googlecaptcha")
    }

    private fun blazingFastFinalResolver(response: String, url: String){
        Handler(Looper.getMainLooper()).post {
            try {
                Log.e("IClient", "try blazingFastFinalResolver ${response.length}, $url")
                context.startActivity(Intent(context, BlazingFastActivity::class.java).apply {
                    putExtra("content", response)
                    putExtra("url", url)
                }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (ex: Exception) {
                Log.e("IClient", "catch blazingFastFinalResolver ${ex.message}")
                ex.printStackTrace()
            }
        }
        throw Exception("blazingfast")
    }

    private fun blazingFastResolver(response: String): Boolean {
        val matcher = blazingFastPattern.matcher(response)
        if (matcher.find()) {
            Log.e("IClient", "blazingFastResolver before: ${matcher.group(1)}")
            val width = context.resources.displayMetrics.widthPixels
            val newUrl = jsConcatStringPattern.matcher(matcher.group(1)).replaceAll(width.toString())
            /*Single
                    .fromCallable {

                    }
                    .delay(4000, TimeUnit.MILLISECONDS)
                    .subscribe()*/

            val httpUrl: HttpUrl = HttpUrl.parse("${Api.BASE_URL}$newUrl")
                    ?: throw Exception("URL incorrect")

            val request = Request.Builder()
                    .url(httpUrl)
                    .method(METHOD_GET, null)
                    .build()

            var okHttpResponse: Response? = null
            var responseBody: ResponseBody? = null
            try {
                okHttpResponse = client.newCall(request).execute()
                if (!okHttpResponse!!.isSuccessful)
                    throw IOException("Unexpected code $okHttpResponse")

                responseBody = okHttpResponse.body()
                val responseString = responseBody?.string().orEmpty()
                val redirectString = okHttpResponse.request().url().toString()
                Log.e("IClient", "blazingFastResolver after: ${responseString}")
                blazingFastFinalResolver(responseString, redirectString)

            } finally {
                okHttpResponse?.close()
                responseBody?.close()
            }

            return true
        }
        return false
    }
}
