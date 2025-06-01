package ru.radiationx.data.app.donation

import okhttp3.FormBody
import retrofit2.HttpException
import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.donation.models.YooMoneyDialog
import ru.radiationx.data.app.donation.remote.DonationInfoResponse
import ru.radiationx.shared.ktx.sequentialFirstNotFailure
import javax.inject.Inject

class DonationApiDataSource @Inject constructor(
    private val api: DirectApi
) {

    private val urls = listOf(
        "https://raw.githubusercontent.com/anilibria/anilibria-app/master/donations-config.json",
        "https://bitbucket.org/RadiationX/anilibria-app/raw/master/donations-config.json"
    )

    suspend fun getDonationDetail(): DonationInfoResponse {
        return urls.sequentialFirstNotFailure { url ->
            api.getDonationConfig(url)
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

        val body = FormBody.Builder().apply {
            params.forEach {
                add(it.key, it.value)
            }
        }.build()

        val response = api.getYooMoney("https://yoomoney.ru/quickpay/confirm.xml", body)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        // redirect
        return response.raw().request.url.toString()
    }
}