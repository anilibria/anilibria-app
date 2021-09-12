package ru.radiationx.anilibria.presentation.auth.social

import android.webkit.CookieManager
import java.util.*
import java.util.concurrent.TimeUnit

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

    fun clearCookies() {
        CookieManager.getInstance().removeAllCookie()
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