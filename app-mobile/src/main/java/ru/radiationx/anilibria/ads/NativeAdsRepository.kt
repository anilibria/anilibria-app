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
            override fun onAdLoaded(p0: NativeAd) {
                continuation.resume(p0)
            }

            override fun onAdFailedToLoad(p0: AdRequestError) {
                continuation.resumeWithException(Exception("${p0.code}, ${p0.description}"))
            }
        })
        continuation.invokeOnCancellation {
            loader.cancelLoading()
            loader.setNativeAdLoadListener(null)
        }
        loader.loadAd(request)
    }
}