package ru.radiationx.anilibria.ads

import android.app.Activity
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.AdTheme
import com.yandex.mobile.ads.common.ImpressionData
import ru.radiationx.anilibria.apptheme.AppTheme
import ru.radiationx.data.ads.domain.BannerAdConfig
import kotlin.math.roundToInt

class BannerAdController(
    private val activity: Activity,
    private val adView: BannerAdView,
    private val adContainer: View,
) {

    private val _adSize: BannerAdSize
        get() {
            val displayMetrics = adContainer.resources.displayMetrics
            var adWidthPixels = adContainer.width
            if (adWidthPixels == 0) {
                adWidthPixels = displayMetrics.widthPixels
            }
            val adWidth = (adWidthPixels / displayMetrics.density).roundToInt()
            return BannerAdSize.fixedSize(adContainer.context, adWidth, 60)
        }

    private val eventListener = object : BannerAdEventListener {
        override fun onAdLoaded() {
            if (activity.isDestroyed) {
                adView.destroy()
                return
            }
        }

        override fun onAdFailedToLoad(adRequestError: AdRequestError) {
            adContainer.isVisible = false
        }

        override fun onAdClicked() {
        }

        override fun onLeftApplication() {
        }

        override fun onReturnedToApplication() {
        }

        override fun onImpression(impressionData: ImpressionData?) {
        }
    }

    init {
        adView.setBannerAdEventListener(eventListener)
    }

    fun load(config: BannerAdConfig, theme: AppTheme) {
        adContainer.isVisible = config.enabled
        if (config.enabled) {
            val adTheme = when (theme) {
                AppTheme.LIGHT -> AdTheme.LIGHT
                AppTheme.DARK -> AdTheme.DARK
            }
            adContainer.doOnLayout {
                loadBannerAd(config, adTheme)
            }
        }
    }

    fun destroy() {
        adView.setBannerAdEventListener(null)
        adView.destroy()
    }

    private fun loadBannerAd(config: BannerAdConfig, adTheme: AdTheme) {
        adView.apply {
            setAdSize(_adSize)
            setAdUnitId(config.unitId)
            loadAd(
                AdRequest.Builder()
                    .setPreferredTheme(adTheme)
                    .setContextTags(config.contextTags)
                    .build()
            )
        }
    }
}