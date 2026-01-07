package ru.radiationx.data.network.errors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class OkHttpErrorInterceptor @Inject constructor() : Interceptor {

    companion object {
        const val X_CON_IP = "X-Con-Ip"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request()).newBuilder().apply {
                chain.connection()?.also {
                    header(X_CON_IP, it.socket().inetAddress.toString())
                }
            }.build()
        } catch (ioe: IOException) {
            val request = chain.request()
            val context = NetworkErrorContext.of(request, null, chain.connection())
            throw OkHttpException(context, ioe)
        }
    }
}