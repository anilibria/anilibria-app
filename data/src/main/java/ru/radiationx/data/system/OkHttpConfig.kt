package ru.radiationx.data.system

import android.os.Build
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import java.lang.Exception
import java.security.cert.X509Certificate
import javax.net.ssl.*

object OkHttpConfig {

    class DummyHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            //Log.e("DummyHost", "verify $hostname, $session")
            return true
        }
    }

    class DummyTrustManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            //Log.e("DummyTrust", "checkClientTrusted ${chain?.size}, $authType")
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            //Log.e("DummyTrust", "checkServerTrusted ${chain?.size}, $authType")
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            //Log.e("DummyTrust", "getAcceptedIssuers")
            return emptyArray()
        }
    }
}

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
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        try {
            val trustManager = OkHttpConfig.DummyTrustManager()
            sslSocketFactory(TLSSocketFactory(), trustManager)
            hostnameVerifier(OkHttpConfig.DummyHostnameVerifier())
        } catch (ex: Exception) {
            //Log.e("DummyException", "Hello: ${ex.message}")
            ex.printStackTrace()
        }
    }
    return this
}
