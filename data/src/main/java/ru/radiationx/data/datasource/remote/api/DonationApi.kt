package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog
import ru.radiationx.data.entity.response.donation.DonationInfoResponse
import javax.inject.Inject

class DonationApi @Inject constructor(
    @ApiClient private val client: IClient,
    @MainClient private val mainClient: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun getDonationDetail(): DonationInfoResponse {
        val args: Map<String, String> = mapOf(
            "query" to "donation_details"
        )
        return client
            .post(apiConfig.apiUrl, args)
            .fetchApiResponse(moshi)
    }

    // Doc https://yoomoney.ru/docs/payment-buttons/using-api/forms
    suspend fun createYooMoneyPayLink(
        amount: Int,
        type: String,
        form: YooMoneyDialog.YooMoneyForm
    ): String {
        val yooMoneyType = when (type) {
            YooMoneyDialog.TYPE_ID_ACCOUNT -> "PC"
            YooMoneyDialog.TYPE_ID_CARD -> "AC"
            YooMoneyDialog.TYPE_ID_MOBILE -> "MC"
            else -> null
        }
        val params = mapOf(
            "receiver" to form.receiver,
            "quickpay-form" to "shop",
            "targets" to form.target,
            "paymentType" to yooMoneyType.orEmpty(),
            "sum" to amount.toString(),
            "formcomment" to form.shortDesc.orEmpty(),
            "short-dest" to form.shortDesc.orEmpty(),
            "label" to form.label.orEmpty()
        )

        return mainClient
            .postFull("https://yoomoney.ru/quickpay/confirm.xml", params)
            .redirect
    }
}