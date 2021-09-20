package ru.radiationx.anilibria.ui.fragments.donation.adapter

import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.data.entity.domain.donation.*

data class DonationHeaderListItem(val data: DonationContentHeader) : ListItem(data.text)
data class DonationCaptionListItem(val data: DonationContentCaption) : ListItem(data.text)
data class DonationButtonListItem(val data: DonationContentButton) : ListItem(data.text)
data class DonationSectionListItem(
    val data: DonationContentSection
) : ListItem("${data.title}_${data.subtitle}")

data class DonationDividerListItem(val id: Any, val data: DonationContentDivider) : ListItem(id)