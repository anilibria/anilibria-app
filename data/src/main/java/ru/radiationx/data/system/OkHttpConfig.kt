package ru.radiationx.data.system

import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

fun OkHttpClient.Builder.appendConnectionSpecs(): OkHttpClient.Builder {
    val cipherSuites = mutableListOf<CipherSuite>()
    val suites = ConnectionSpec.MODERN_TLS.cipherSuites
    suites?.also { cipherSuites.addAll(it) }

    if (!cipherSuites.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)) {
        cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
    }

    if (!cipherSuites.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA)) {
        cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA)
    }

    val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .cipherSuites(*cipherSuites.toTypedArray())
        .build()
    connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
    return this
}

fun OkHttpClient.Builder.appendTimeouts(): OkHttpClient.Builder {
    callTimeout(25, TimeUnit.SECONDS)
    connectTimeout(15, TimeUnit.SECONDS)
    readTimeout(15, TimeUnit.SECONDS)
    writeTimeout(15, TimeUnit.SECONDS)
    return this
}
