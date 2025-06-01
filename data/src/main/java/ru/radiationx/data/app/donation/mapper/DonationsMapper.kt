package ru.radiationx.data.app.donation.mapper

import ru.radiationx.data.app.donation.mapper.yoomoney.toDomain
import ru.radiationx.data.app.donation.models.DonationCard
import ru.radiationx.data.app.donation.models.DonationContentButton
import ru.radiationx.data.app.donation.models.DonationContentCaption
import ru.radiationx.data.app.donation.models.DonationContentDivider
import ru.radiationx.data.app.donation.models.DonationContentHeader
import ru.radiationx.data.app.donation.models.DonationContentItem
import ru.radiationx.data.app.donation.models.DonationContentSection
import ru.radiationx.data.app.donation.models.DonationDialog
import ru.radiationx.data.app.donation.models.DonationInfo
import ru.radiationx.data.app.donation.remote.DonationCardResponse
import ru.radiationx.data.app.donation.remote.DonationContentItemResponse
import ru.radiationx.data.app.donation.remote.DonationInfoResponse
import ru.radiationx.data.app.donation.remote.content.DonationContentButtonResponse
import ru.radiationx.data.app.donation.remote.content.DonationContentCaptionResponse
import ru.radiationx.data.app.donation.remote.content.DonationContentDividerResponse
import ru.radiationx.data.app.donation.remote.content.DonationContentHeaderResponse
import ru.radiationx.data.app.donation.remote.content.DonationContentSectionResponse
import ru.radiationx.data.app.donation.remote.content_data.DonationDialogResponse
import ru.radiationx.data.common.toAbsoluteUrl

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
