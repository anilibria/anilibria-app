package ru.radiationx.anilibria.ui.fragments.donation.adapter

import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationContentCaption
import ru.radiationx.data.entity.domain.donation.DonationContentDivider
import ru.radiationx.data.entity.domain.donation.DonationContentHeader
import ru.radiationx.data.entity.domain.donation.DonationContentSection

data class DonationHeaderListItem(val data: DonationContentHeader) : ListItem(data.text)
data class DonationCaptionListItem(val data: DonationContentCaption) : ListItem(data.text)
data class DonationButtonListItem(val data: DonationContentButton) : ListItem(data.text)
data class DonationSectionListItem(
    val data: DonationContentSection
) : ListItem("${data.title}_${data.subtitle}")

data class DonationDividerListItem(val id: Any, val data: DonationContentDivider) : ListItem(id)