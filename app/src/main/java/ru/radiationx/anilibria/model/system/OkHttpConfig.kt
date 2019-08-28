package ru.radiationx.anilibria.model.system

import android.util.Log
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import javax.net.ssl.HttpsURLConnection

object OkHttpConfig {
    val connectionSpec: List<ConnectionSpec> by lazy {

        // Add legacy cipher suite for Android 4
        val cipherSuites = mutableListOf<CipherSuite>()
        val suites = ConnectionSpec.MODERN_TLS.cipherSuites()
        suites?.also { cipherSuites.addAll(it) }

        if (!cipherSuites.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)) {
            cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
        }

        if (!cipherSuites.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA)) {
            cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA)
        }

        //cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256)

        Log.e("lalala", "connectionSpec : ${cipherSuites.joinToString { it.javaName() }}")
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .cipherSuites(*cipherSuites.toTypedArray())
                .build()
        listOf(spec, ConnectionSpec.CLEARTEXT)
    }
}