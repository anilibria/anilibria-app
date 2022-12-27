package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.response.donation.content_data.YooMoneyDialogResponse
import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog

fun YooMoneyDialogResponse.toDomain() = YooMoneyDialog(
    title,
    help,
    amounts?.toDomain(),
    paymentTypes.toDomain(),
    form.toDomain(),
    btDonateText,
    btCancelText
)

fun YooMoneyDialogResponse.Amounts.toDomain() = YooMoneyDialog.Amounts(
    title = title,
    hint = hint,
    defaultValue = defaultValue,
    items = items
)

fun YooMoneyDialogResponse.PaymentTypes.toDomain() = YooMoneyDialog.PaymentTypes(
    title = title,
    selectedId = selectedId,
    items = items.map { it.toDomain() }
)

fun YooMoneyDialogResponse.PaymentType.toDomain() = YooMoneyDialog.PaymentType(
    id = id,
    title = title
)

fun YooMoneyDialogResponse.YooMoneyForm.toDomain() = YooMoneyDialog.YooMoneyForm(
    receiver = receiver,
    target = target,
    shortDesc = shortDesc,
    label = label
)