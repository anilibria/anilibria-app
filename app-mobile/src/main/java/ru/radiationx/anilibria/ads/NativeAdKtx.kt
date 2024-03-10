package ru.radiationx.anilibria.ads

import com.yandex.mobile.ads.nativeads.NativeAd


sealed class NativeAdItem<T> {
    data class Ad<T>(val ad: NativeAd) : NativeAdItem<T>()
    data class Data<T>(val data: T) : NativeAdItem<T>()
}

fun <T, R> NativeAdItem<T>.convert(block: (T) -> R): NativeAdItem<R> = when (this) {
    is NativeAdItem.Ad -> NativeAdItem.Ad(ad)
    is NativeAdItem.Data -> NativeAdItem.Data(block.invoke(data))
}

fun <T> List<T>.addAdAt(index: Int, ad: NativeAd?): List<NativeAdItem<T>> {
    val result = mutableListOf<NativeAdItem<T>>()
    forEach { result.add(NativeAdItem.Data(it)) }
    val adInsertIndex = index.coerceIn(0, result.size)
    if (ad != null) {
        result.add(adInsertIndex, NativeAdItem.Ad(ad))
    }
    return result
}