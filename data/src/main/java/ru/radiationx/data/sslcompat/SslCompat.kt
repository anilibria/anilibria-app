package ru.radiationx.data.sslcompat

import android.content.Context
import android.os.Build
import okhttp3.ConnectionSpec
import org.conscrypt.Conscrypt
import java.security.KeyStore
import java.security.Provider
import java.security.Security
import java.security.cert.CertificateFactory
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class SslCompat @Inject constructor(
    private val context: Context,
    private val rawCertResources: List<Int>,
    private val connectionSpecs: List<ConnectionSpec>,
) {

    val data: Result<Data> by lazy {
        runCatching {
            val conscrypt = Conscrypt.newProvider()
            Security.insertProviderAt(conscrypt, 1)

            val trustManager = createTrustManager()
            val sslContext = SSLContext.getInstance("TLS", conscrypt).apply {
                init(null, arrayOf<TrustManager>(trustManager), null)
            }
            val tlsVersions = connectionSpecs.flatMap {
                it.tlsVersions.orEmpty()
            }
            val socketFactory = CompatSSLSocketFactory(sslContext.socketFactory, tlsVersions)
            Data(connectionSpecs, conscrypt, trustManager, socketFactory)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun createTrustManager(): X509TrustManager {
        val defaultTrustManager = Conscrypt.getDefaultX509TrustManager()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return defaultTrustManager
        }
        val compatKeyStore = createCompatKeyStore()
        return CompatTrustManager(
            defaultManager = defaultTrustManager,
            additionalKeyStores = listOfNotNull(compatKeyStore)
        )
    }

    private fun createCompatKeyStore(): KeyStore? {
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
        }

        val factory = CertificateFactory.getInstance("X.509")
        rawCertResources.forEach { rawResource ->
            val alias = try {
                context.resources.getResourceName(rawResource)
            } catch (ex: Exception) {
                "$rawResource"
            }
            val certificate = context.resources.openRawResource(rawResource).use {
                factory.generateCertificate(it)
            }
            keyStore.setCertificateEntry(alias, certificate)
        }
        return keyStore
    }

    class Data(
        val connectionSpec: List<ConnectionSpec>,
        val conscrypt: Provider,
        val trustManager: X509TrustManager,
        val socketFactory: SSLSocketFactory,
    ) {
        override fun toString(): String {
            return conscrypt.toString()
        }
    }
}