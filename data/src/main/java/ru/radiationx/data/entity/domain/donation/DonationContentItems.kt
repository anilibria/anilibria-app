package ru.radiationx.data.entity.domain.donation

import ru.radiationx.data.entity.common.Url

sealed class DonationContentItem

data class DonationContentButton(
    val tag: String?,
    val text: String,
    val link: Url.Absolute?,
    val brand: String?,
    val icon: String?
) : DonationContentItem()

data class DonationContentCaption(
    val text: String
) : DonationContentItem()

data class DonationContentDivider(
    val height: Int
) : DonationContentItem()

data class DonationContentHeader(
    val text: String
) : DonationContentItem()

data class DonationContentSection(
    val title: String?,
    val subtitle: String?
) : DonationContentItem()