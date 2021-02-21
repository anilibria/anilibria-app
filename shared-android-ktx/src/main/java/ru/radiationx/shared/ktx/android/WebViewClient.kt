package ru.radiationx.shared.ktx.android

import android.net.http.SslError
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse
import androidx.annotation.RequiresApi
import java.lang.Exception

fun SslError?.toException() = Exception("onReceivedSslError $this")

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun WebResourceResponse?.toException() = Exception("onReceivedHttpError ${this?.reasonPhrase.orEmpty()}")

@RequiresApi(Build.VERSION_CODES.M)
fun WebResourceError?.toException() = Exception("onReceivedError ${this?.description?.toString().orEmpty()}")