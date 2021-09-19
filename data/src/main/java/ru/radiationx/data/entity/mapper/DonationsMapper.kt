package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.app.donation.DonationCardResponse
import ru.radiationx.data.entity.app.donation.DonationContentDataResponse
import ru.radiationx.data.entity.app.donation.DonationContentItemResponse
import ru.radiationx.data.entity.app.donation.DonationInfoResponse
import ru.radiationx.data.entity.app.donation.content.*
import ru.radiationx.data.entity.app.donation.content_data.DonationDialogResponse
import ru.radiationx.data.entity.domain.donation.*

fun DonationInfoResponse.toDomain() = DonationInfo(
    cardNewDonations = cards.newDonations?.toDomain(),
    cardRelease = cards.release?.toDomain(),
    detailContent = detail.content.toDomain(),
    detailContentData = detail.contentData.toDomain()
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
    link = link,
    brand = brand
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

fun DonationContentDataResponse.toDomain() = DonationContentData(
    joinTeamDialog = joinTeamDialog?.toDomain(),
    infraDialog = infraDialog?.toDomain(),
    yooMoneyDialog = yooMoneyDialog?.toDomain()
)

fun DonationDialogResponse.toDomain() = DonationDialog(
    content = content.toDomain(),
    cancelText = cancelText
)
