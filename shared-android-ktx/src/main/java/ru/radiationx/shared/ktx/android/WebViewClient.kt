package ru.radiationx.shared.ktx.android

import android.net.http.SslError
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.annotation.RequiresApi
import java.lang.Exception

fun SslError?.toException(): Exception {
    return Exception("onReceivedSslError $this")
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun WebResourceResponse?.toException(request: WebResourceRequest?): Exception {
    val reason = this?.reasonPhrase.toString()
    val url = request?.url.toString()
    return Exception("onReceivedHttpError reason='$reason', url='$url'")
}

@RequiresApi(Build.VERSION_CODES.M)
fun WebResourceError?.toException(request: WebResourceRequest?): Exception {
    val description =this?.description.toString()
    val errorCode = this?.errorCode.toString()
    val url = request?.url.toString()
    return Exception("onReceivedError desc='$description', code='$errorCode', url='$url'")
}