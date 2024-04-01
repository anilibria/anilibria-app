package ru.radiationx.data.sslcompat

import android.annotation.SuppressLint
import android.util.Log
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@SuppressLint("CustomX509TrustManager")
class CompatTrustManager(
    private val defaultManager: X509TrustManager,
    private val additionalKeyStores: List<KeyStore>,
) : X509TrustManager {

    private val managers = mutableListOf(defaultManager)

    init {
        for (keyStore in additionalKeyStores) {
            val algorithm = TrustManagerFactory.getDefaultAlgorithm()
            val factory = TrustManagerFactory.getInstance(algorithm)
            factory.init(keyStore)
            factory.trustManagers.forEach { manager ->
                if (manager is X509TrustManager) {
                    managers.add(manager)
                }
            }
        }
    }

    /*
     * Delegate to the default trust manager.
     */
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        defaultManager.checkClientTrusted(chain, authType)
    }

    /*
     * Loop over the trustmanagers until we find one that accepts our server
     */
    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        for (tm in managers) {
            try {
                tm.checkServerTrusted(chain, authType)
                return
            } catch (e: CertificateException) {
                // ignore
            }
        }
        throw CertificateException()
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val certificates = managers.flatMap {
            it.acceptedIssuers.toList()
        }
        return certificates.toTypedArray()
    }
}
