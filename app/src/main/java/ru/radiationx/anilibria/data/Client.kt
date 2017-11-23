package ru.radiationx.anilibria.data;

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/* Created by radiationx on 31.10.17. */

class Client {
    private object Holder {
        val INSTANCE = Client()
    }

    companion object {
        val instance: Client by lazy { Holder.INSTANCE }
    }

    private val client = OkHttpClient()

    @Throws(Exception::class)
    operator fun get(url: String): String? {
        val request = Request.Builder()
                .url(url)
                .build()
        var okHttpResponse: Response? = null
        var responseText: String? = null
        try {
            Log.e("SUKA", "Client get: " + url)
            okHttpResponse = client.newCall(request).execute()
            if (!okHttpResponse!!.isSuccessful)
                throw IOException("Unexpected code " + okHttpResponse)

            val responseBody = okHttpResponse.body()
            responseText = responseBody!!.string()
        } finally {
            if (okHttpResponse != null)
                okHttpResponse.close()
        }

        return responseText
    }
}
