package ru.radiationx.data.network.errors

import okhttp3.Connection
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import retrofit2.Invocation


sealed class NetworkException(
    val context: NetworkErrorContext,
    cause: Throwable
) : IOException(cause) {

    override fun toString(): String {
        return "${super.toString()}\ncontext: $context"
    }
}

class OkHttpException(
    context: NetworkErrorContext,
    cause: Throwable
) : NetworkException(context, cause)

class RetrofitCallException(
    context: NetworkErrorContext,
    cause: Throwable
) : NetworkException(context, cause)

data class NetworkErrorContext(
    val request: Request,
    val response: Response?,
    val connection: Connection?,
    val invocation: Invocation?
) {

    companion object {
        fun of(
            request: okhttp3.Request,
            response: okhttp3.Response?,
            connection: okhttp3.Connection?,
        ): NetworkErrorContext {
            return NetworkErrorContext(
                request = Request(request.method, request.url.toString()),
                response = response?.let {
                    Response(
                        code = it.code
                    )
                },
                connection = Connection.of(connection, response),
                invocation = request.tag(Invocation::class.java)
            )
        }
    }

    data class Request(
        val method: String,
        val url: String,
    )

    data class Response(
        val code: Int
    )

    data class Connection(
        val ip: String,
        val protocol: String
    ) {

        companion object {
            fun of(connection: okhttp3.Connection?, response: okhttp3.Response?): Connection? {
                if (connection != null) {
                    return Connection(
                        ip = connection.socket().inetAddress.toString(),
                        protocol = connection.protocol().name
                    )
                }
                if (response != null) {
                    return Connection(
                        ip = response.header(OkHttpErrorInterceptor.X_CON_IP) ?: "Unknown",
                        protocol = response.protocol.name
                    )
                }
                return null
            }
        }
    }
}