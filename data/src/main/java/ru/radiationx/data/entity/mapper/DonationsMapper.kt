package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.common.toAbsoluteUrl
import ru.radiationx.data.entity.domain.donation.DonationCard
import ru.radiationx.data.entity.domain.donation.DonationContentButton
import ru.radiationx.data.entity.domain.donation.DonationContentCaption
import ru.radiationx.data.entity.domain.donation.DonationContentDivider
import ru.radiationx.data.entity.domain.donation.DonationContentHeader
import ru.radiationx.data.entity.domain.donation.DonationContentItem
import ru.radiationx.data.entity.domain.donation.DonationContentSection
import ru.radiationx.data.entity.domain.donation.DonationDialog
import ru.radiationx.data.entity.domain.donation.DonationInfo
import ru.radiationx.data.entity.response.donation.DonationCardResponse
import ru.radiationx.data.entity.response.donation.DonationContentItemResponse
import ru.radiationx.data.entity.response.donation.DonationInfoResponse
import ru.radiationx.data.entity.response.donation.content.DonationContentButtonResponse
import ru.radiationx.data.entity.response.donation.content.DonationContentCaptionResponse
import ru.radiationx.data.entity.response.donation.content.DonationContentDividerResponse
import ru.radiationx.data.entity.response.donation.content.DonationContentHeaderResponse
import ru.radiationx.data.entity.response.donation.content.DonationContentSectionResponse
import ru.radiationx.data.entity.response.donation.content_data.DonationDialogResponse

fun DonationInfoResponse.toDomain() = DonationInfo(
    cardNewDonations = cards.newDonations?.toDomain(),
    cardRelease = cards.release?.toDomain(),
    detailContent = detail.content.toDomain(),
    contentDialogs = detail.contentDialogs.map { it.toDomain() },
    yooMoneyDialog = detail.yooMoneyDialog?.toDomain()
)

fun DonationCardResponse.toDomain() = DonationCard(
    title = title,
    subtitle = subtitle
)

fun List<DonationContentItemResponse>.toDomain() = mapNotNull { it.toDomain() }

fun DonationContentItemResponse.toDomain(): DonationContentItem? = when (type) {
    "button" -> requireNotNull(button).toDomain()
    "caption" -> requireNotNull(caption).toDomain()
    "divider" -> requireNotNull(divider).toDomain()
    "header" -> requireNotNull(header).toDomain()
    "section" -> requireNotNull(section).toDomain()
    else -> null
}

fun DonationContentButtonResponse.toDomain() = DonationContentButton(
    tag = tag,
    text = text,
    link = link?.toAbsoluteUrl(),
    brand = brand,
    icon = icon
)

fun DonationContentCaptionResponse.toDomain() = DonationContentCaption(
    text = text
)

fun DonationContentDividerResponse.toDomain() = DonationContentDivider(
    height = height
)

fun DonationContentHeaderResponse.toDomain() = DonationContentHeader(
    text = text
)

fun DonationContentSectionResponse.toDomain() = DonationContentSection(
    title = title,
    subtitle = subtitle
)

fun DonationDialogResponse.toDomain() = DonationDialog(
    tag = tag,
    content = content.toDomain(),
    cancelText = cancelText
)
