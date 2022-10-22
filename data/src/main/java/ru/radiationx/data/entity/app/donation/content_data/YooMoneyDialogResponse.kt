package ru.radiationx.data.entity.app.donation.content_data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YooMoneyDialogResponse(
    @Json(name = "title")
    val title: String,
    @Json(name = "help")
    val help: String?,
    @Json(name = "amounts")
    val amounts: Amounts?,
    @Json(name = "payment_types")
    val paymentTypes: PaymentTypes,
    @Json(name = "form")
    val form: YooMoneyForm,
    @Json(name = "bt_donate_text")
    val btDonateText: String,
    @Json(name = "bt_cancel_text")
    val btCancelText: String
) {

    @JsonClass(generateAdapter = true)
    data class Amounts(
        @Json(name = "title")
        val title: String,
        @Json(name = "hint")
        val hint: String,
        @Json(name = "default_value")
        val defaultValue: Int?,
        @Json(name = "items")
        val items: List<Int>
    )

    @JsonClass(generateAdapter = true)
    data class PaymentTypes(
        @Json(name = "title")
        val title: String,
        @Json(name = "selected_id")
        val selectedId: String?,
        @Json(name = "items")
        val items: List<PaymentType>
    )

    @JsonClass(generateAdapter = true)
    data class PaymentType(
        @Json(name = "id")
        val id: String,
        @Json(name = "title")
        val title: String
    )

    @JsonClass(generateAdapter = true)
    data class YooMoneyForm(
        @Json(name = "receiver")
        val receiver: String,
        @Json(name = "target")
        val target: String,
        @Json(name = "short_desc")
        val shortDesc: String?,
        @Json(name = "label")
        val label: String?
    )
}
