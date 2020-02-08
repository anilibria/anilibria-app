package ru.radiationx.anilibria.model.system

import android.util.Log
import io.reactivex.Single
import okhttp3.*
import ru.radiationx.anilibria.model.datasource.remote.IClient
import ru.radiationx.anilibria.model.datasource.remote.NetworkResponse
import ru.radiationx.anilibria.model.datasource.remote.address.ApiConfig
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
        const val USER_AGENT = "mobileApp Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.170 Safari/537.36 OPR/53.0.2907.68"
    }

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
            okHttpResponse = clientWrapper.get().newCall(request).execute()

            if (!okHttpResponse!!.isSuccessful) {
                throw HttpException(okHttpResponse.code(), okHttpResponse.message(), okHttpResponse)
                //throw IOException("Unexpected code $okHttpResponse")
            }
            responseBody = okHttpResponse.body()

            response.hostIp = okHttpResponse.headers(HEADER_HOST_IP)?.firstOrNull()
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
