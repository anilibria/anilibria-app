package ru.radiationx.shared.ktx.android

import android.net.http.SslError
import android.webkit.WebResourceResponse

fun SslError.toException(): Exception {
    return Exception("onReceivedSslError $this")
}

fun WebResourceResponse.toException(request: WebResourceRequestCompat): Exception {
    return Exception("onReceivedHttpError reason='$reasonPhrase', url='${request.url}'")
}

fun WebResourceErrorCompat.toException(request: WebResourceRequestCompat): Exception {
    return Exception("onReceivedError desc='$description', code='$errorCode', url='${request.url}'")
}