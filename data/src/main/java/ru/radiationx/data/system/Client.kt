package ru.radiationx.data.system

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.NetworkResponse
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


open class Client @Inject constructor(
    private val clientWrapper: ClientWrapper,
    private val sharedBuildConfig: SharedBuildConfig,
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

    override suspend fun get(url: String, args: Map<String, String>): String =
        requireNotNull(getFull(url, args).body)

    override suspend fun post(url: String, args: Map<String, String>): String =
        requireNotNull(postFull(url, args).body)

    override suspend fun put(url: String, args: Map<String, String>): String =
        requireNotNull(putFull(url, args).body)

    override suspend fun delete(url: String, args: Map<String, String>): String =
        requireNotNull(deleteFull(url, args).body)

    override suspend fun getFull(url: String, args: Map<String, String>): NetworkResponse =
        request(METHOD_GET, url, args)

    override suspend fun postFull(url: String, args: Map<String, String>): NetworkResponse =
        request(METHOD_POST, url, args)

    override suspend fun putFull(url: String, args: Map<String, String>): NetworkResponse =
        request(METHOD_PUT, url, args)

    override suspend fun deleteFull(url: String, args: Map<String, String>): NetworkResponse =
        request(METHOD_DELETE, url, args)

    override suspend fun getRaw(url: String, args: Map<String, String>): Response =
        requestRaw(METHOD_GET, url, args)

    override suspend fun postRaw(url: String, args: Map<String, String>): Response =
        requestRaw(METHOD_POST, url, args)

    private suspend fun request(
        method: String,
        url: String,
        args: Map<String, String>,
    ): NetworkResponse {
        val callResponse = requestRaw(method, url, args)
        return NetworkResponse(
            getHttpUrl(url, method, args).toString(),
            callResponse.code,
            callResponse.message,
            callResponse.request.url.toString(),
            callResponse.body?.string().orEmpty(),
            callResponse.headers(HEADER_HOST_IP).firstOrNull()
        )
    }

    private suspend fun requestRaw(
        method: String,
        url: String,
        args: Map<String, String>,
    ): Response {
        return withContext(Dispatchers.IO) {
            val body = getRequestBody(method, args)
            val httpUrl = getHttpUrl(url, method, args)
            val request = Request.Builder()
                .url(httpUrl)
                .method(method, body)
                .build()

            val call = clientWrapper.get().newCall(request)
            val callResponse = call.awaitResponse()
            if (!callResponse.isSuccessful) {
                throw HttpException(callResponse.code, callResponse.message, callResponse)
            }
            callResponse
        }
    }

    private fun getRequestBody(
        method: String,
        args: Map<String, String>,
    ): RequestBody? = when (method) {
        METHOD_POST, METHOD_PUT -> {
            FormBody.Builder()
                .apply {
                    args.forEach {
                        add(it.key, it.value)
                    }
                }
                .build()
        }

        METHOD_GET, METHOD_DELETE -> null
        else -> throw Exception("Unknown method: $method")
    }

    private fun getHttpUrl(url: String, method: String, args: Map<String, String>): HttpUrl {
        var httpUrl = url.toHttpUrlOrNull() ?: throw Exception("URL incorrect: '$url'")
        if (sharedBuildConfig.debug || method == METHOD_GET) {
            httpUrl = httpUrl.newBuilder().let { builder ->
                args.forEach { builder.addQueryParameter(it.key, it.value) }
                builder.build()
            }
        }
        return httpUrl
    }

    private suspend fun Call.awaitResponse(): Response {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                cancel()
            }
            enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
        }
    }
}
