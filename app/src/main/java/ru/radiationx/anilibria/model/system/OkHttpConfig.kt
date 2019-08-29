package ru.radiationx.anilibria.model.system

import android.os.Build
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import ru.radiationx.anilibria.App
import javax.net.ssl.HttpsURLConnection
import android.widget.TextView
import android.view.ViewGroup
import okhttp3.OkHttpClient
import java.lang.Exception
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException


fun OkHttpClient.Builder.appendConnectionSpecs(): OkHttpClient.Builder {
    val cipherSuites = mutableListOf<CipherSuite>()
    val suites = ConnectionSpec.MODERN_TLS.cipherSuites()
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

fun OkHttpClient.Builder.appendSocketFactoryIfNeeded(): OkHttpClient.Builder {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
        try {
            this.sslSocketFactory(TLSSocketFactory()).build()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    return this
}
