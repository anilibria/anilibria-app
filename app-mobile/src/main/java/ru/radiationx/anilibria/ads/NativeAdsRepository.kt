package ru.radiationx.anilibria.ads

import android.content.Context
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeAdLoader
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NativeAdsRepository @Inject constructor(
    private val context: Context,
) {

    suspend fun load(
        request: NativeAdRequestConfiguration,
    ): NativeAd = suspendCancellableCoroutine { continuation ->
        val loader = NativeAdLoader(context)
        loader.setNativeAdLoadListener(object : NativeAdLoadListener {
            override fun onAdLoaded(nativeAd: NativeAd) {
                continuation.resume(nativeAd)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                continuation.resumeWithException(Exception("${error.code}, ${error.description}"))
            }
        })
        continuation.invokeOnCancellation {
            loader.cancelLoading()
            loader.setNativeAdLoadListener(null)
        }
        loader.loadAd(request)
    }
}