package ru.radiationx.data.entity.app.donation.content_data

import com.google.gson.annotations.SerializedName

data class YooMoneyDialogResponse(
    @SerializedName("title")
    val title: String,
    @SerializedName("help")
    val help: String?,
    @SerializedName("amounts")
    val amounts: Amounts?,
    @SerializedName("payment_types")
    val paymentTypes: PaymentTypes,
    @SerializedName("form")
    val form: YooMoneyForm,
    @SerializedName("bt_donate_text")
    val btDonateText: String,
    @SerializedName("bt_cancel_text")
    val btCancelText: String
) {

    data class Amounts(
        @SerializedName("title")
        val title: String,
        @SerializedName("hint")
        val hint: String,
        @SerializedName("default_value")
        val defaultValue: Int?,
        @SerializedName("items")
        val items: List<Int>
    )

    data class PaymentTypes(
        @SerializedName("title")
        val title: String,
        @SerializedName("selected_id")
        val selectedId: String?,
        @SerializedName("items")
        val items: List<PaymentType>
    )

    data class PaymentType(
        @SerializedName("id")
        val id: String,
        @SerializedName("title")
        val title: String
    )

    data class YooMoneyForm(
        @SerializedName("receiver")
        val receiver: String,
        @SerializedName("target")
        val target: String,
        @SerializedName("short_desc")
        val shortDesc: String?,
        @SerializedName("label")
        val label: String?
    )
}
