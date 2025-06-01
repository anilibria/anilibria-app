package ru.radiationx.data.app.donation.mapper.yoomoney

import ru.radiationx.data.app.donation.models.YooMoneyDialog
import ru.radiationx.data.app.donation.remote.content_data.YooMoneyDialogResponse

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