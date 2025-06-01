package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog
import ru.radiationx.data.entity.response.donation.DonationInfoResponse
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class DonationApiDataSource @Inject constructor(
    @MainClient private val mainClient: IClient,
    private val moshi: Moshi
) {

    private val urls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/donations-config.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/donations-config.json"
    )

    suspend fun getDonationDetail(): DonationInfoResponse {
        return urls.sequentialFirstNotFailure { url ->
            mainClient
                .get(url, emptyMap())
                .fetchApiResponse(moshi)
        }
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