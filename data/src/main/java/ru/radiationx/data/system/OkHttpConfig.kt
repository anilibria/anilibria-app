package ru.radiationx.data.system

import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

fun OkHttpClient.Builder.appendTimeouts(): OkHttpClient.Builder {
    callTimeout(25, TimeUnit.SECONDS)
    connectTimeout(15, TimeUnit.SECONDS)
    readTimeout(15, TimeUnit.SECONDS)
    writeTimeout(15, TimeUnit.SECONDS)
    return this
}
