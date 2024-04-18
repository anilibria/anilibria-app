package ru.radiationx.data.sslcompat

import okhttp3.TlsVersion
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class CompatSSLSocketFactory(
    private val mSSLSocketFactory: SSLSocketFactory,
    private val tlsVersions: List<TlsVersion>,
) : SSLSocketFactory() {

    private val protocols = tlsVersions.map { it.javaName }.toTypedArray()

    override fun getDefaultCipherSuites(): Array<String> {
        return mSSLSocketFactory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return mSSLSocketFactory.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket {
        return mSSLSocketFactory.createSocket().enableProtocols()
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return mSSLSocketFactory.createSocket(s, host, port, autoClose).enableProtocols()
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return mSSLSocketFactory.createSocket(host, port).enableProtocols()
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int,
    ): Socket {
        return mSSLSocketFactory.createSocket(host, port, localHost, localPort).enableProtocols()
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return mSSLSocketFactory.createSocket(host, port).enableProtocols()
    }

    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): Socket {
        return mSSLSocketFactory.createSocket(
            address,
            port,
            localAddress,
            localPort
        ).enableProtocols()
    }

    private fun Socket.enableProtocols(): Socket {
        if (this is SSLSocket) {
            enabledProtocols = protocols
        }
        return this
    }
}
