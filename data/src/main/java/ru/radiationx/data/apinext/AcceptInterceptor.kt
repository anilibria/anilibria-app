package ru.radiationx.data.apinext

import okhttp3.Interceptor
import okhttp3.Response

class AcceptInterceptor : Interceptor {

    companion object {
        const val HEADER_ACCEPT = "Accept"
        const val HEADER_CONTENT_TYPE = "Content-Type"
        const val HEADER_CONTENT_VALUE = "application/json"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain
            .request()
            .newBuilder()
            .addHeader(HEADER_CONTENT_TYPE, HEADER_CONTENT_VALUE)
            .addHeader(HEADER_ACCEPT, HEADER_CONTENT_VALUE)

        val request = builder.build()
        return chain.proceed(request)
    }
}