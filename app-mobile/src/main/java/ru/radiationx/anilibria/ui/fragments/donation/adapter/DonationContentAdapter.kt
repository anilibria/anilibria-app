package ru.radiationx.anilibria.ui.fragments.donation.adapter

import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationContentCaption
import ru.radiationx.data.entity.domain.donation.DonationContentDivider
import ru.radiationx.data.entity.domain.donation.DonationContentHeader
import ru.radiationx.data.entity.domain.donation.DonationContentItem
import ru.radiationx.data.entity.domain.donation.DonationContentSection

class DonationContentAdapter(
    buttonClickListener: (DonationContentButton) -> Unit,
    linkClickListener: (String) -> Unit,
) : ListItemAdapter() {

    init {
        addDelegate(DonationButtonDelegate(buttonClickListener))
        addDelegate(DonationCaptionDelegate(linkClickListener))
        addDelegate(DonationDividerDelegate())
        addDelegate(DonationHeaderDelegate())
        addDelegate(DonationSectionDelegate(linkClickListener))
    }

    fun bindState(content: List<DonationContentItem>) {
        val newItems = content.mapNotNull {
            when (it) {
                is DonationContentButton -> DonationButtonListItem(it)
                is DonationContentCaption -> DonationCaptionListItem(it)
                is DonationContentDivider -> {
                    val index = content.indexOf(it)
                    DonationDividerListItem(index, it)
                }

                is DonationContentHeader -> DonationHeaderListItem(it)
                is DonationContentSection -> DonationSectionListItem(it)
                else -> null
            }
        }
        items = newItems
    }
}