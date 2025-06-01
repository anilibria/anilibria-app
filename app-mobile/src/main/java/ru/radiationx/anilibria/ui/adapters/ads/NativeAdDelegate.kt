package ru.radiationx.anilibria.ui.adapters.ads

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yandex.mobile.ads.nativeads.template.HorizontalOffset
import com.yandex.mobile.ads.nativeads.template.NativeBannerView
import com.yandex.mobile.ads.nativeads.template.appearance.BannerAppearance
import com.yandex.mobile.ads.nativeads.template.appearance.ButtonAppearance
import com.yandex.mobile.ads.nativeads.template.appearance.NativeTemplateAppearance
import com.yandex.mobile.ads.nativeads.template.appearance.TextAppearance
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemNativeAdBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.NativeAdListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared.ktx.android.getColorFromAttr

/**
 * Created by radiationx on 13.01.18.
 */
class NativeAdDelegate :
    AppAdapterDelegate<NativeAdListItem, ListItem, NativeAdDelegate.ViewHolder>(
        R.layout.item_native_ad,
        { it is NativeAdListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: NativeAdListItem, holder: ViewHolder) =
        holder.bind(item)

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemNativeAdBinding>()

        private val dimensionsApplier by dimensionsApplier()

        private val appearance by lazy {
            val backgroundColor = binding.root.context.getColorFromAttr(com.google.android.material.R.attr.colorSurface)
            val textDefaultColor = binding.root.context.getColorFromAttr(R.attr.textDefault)
            val textSecondColor = binding.root.context.getColorFromAttr(R.attr.textSecond)
            val textDefault = TextAppearance.Builder().setTextColor(textDefaultColor).build()
            val textSecond = TextAppearance.Builder().setTextColor(textSecondColor).build()
            NativeTemplateAppearance.Builder()
                .withAgeAppearance(textSecond)
                .withBodyAppearance(textDefault)
                .withDomainAppearance(textSecond)
                .withReviewCountAppearance(textSecond)
                .withSponsoredAppearance(textSecond)
                .withTitleAppearance(textDefault)
                .withWarningAppearance(textSecond)
                .withBannerAppearance(
                    BannerAppearance.Builder()
                        .setBackgroundColor(backgroundColor)
                        .setBorderColor(Color.TRANSPARENT)
                        .setBorderWidth(0f)
                        .setContentPadding(HorizontalOffset(8f, 16f))
                        .setImageMargins(HorizontalOffset(-8f, 16f))
                        .build()
                )
                .withCallToActionAppearance(
                    ButtonAppearance.Builder()
                        .setNormalColor(backgroundColor)
                        .setTextAppearance(textDefault)
                        //.setBorderWidth(2f)
                        .build()
                )
                /*.withRatingAppearance()
                .withFaviconAppearance()
                .withImageAppearance()*/
                .build()
        }

        fun bind(item: NativeAdListItem) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            val adview = updateAdView()
            adview.applyAppearance(appearance)
            adview.setAd(item.nativeAd)
        }

        /* Avoid bug with image loading */
        private fun updateAdView(): NativeBannerView {
            val newView = NativeBannerView(binding.root.context)
            binding.root.removeAllViews()
            binding.root.addView(
                newView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            return newView
        }
    }
}