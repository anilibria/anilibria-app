package ru.radiationx.anilibria.ui.fragments.auth.social

import android.webkit.CookieManager
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WebAuthSoFastDetector {
    private val threshold = TimeUnit.SECONDS.toMillis(15)
    private var hasInitialCookies = false
    private var loadTime: Date? = null

    fun reset() {
        hasInitialCookies = false
        loadTime = null
    }

    fun loadUrl(url: String?) {
        hasInitialCookies = CookieManager.getInstance().getCookie(url) != null
        loadTime = Date()
    }

    suspend fun clearCookies(): Boolean {
        return suspendCoroutine { continuation ->
            CookieManager.getInstance().removeAllCookies {
                continuation.resume(it)
            }
        }
    }

    fun isSoFast(): Boolean {
        val successTime = Date()
        val isSmallDelta = loadTime?.let {
            val millisDelta = successTime.time - it.time
            millisDelta < threshold
        } ?: false
        return hasInitialCookies && isSmallDelta
    }
}