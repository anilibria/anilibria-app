package ru.radiationx.data.app

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import ru.radiationx.data.app.ads.remote.AdsConfigDataResponse
import ru.radiationx.data.app.config.remote.ApiConfigResponse
import ru.radiationx.data.app.donation.remote.DonationInfoResponse
import ru.radiationx.data.app.menu.remote.LinkMenuResponse
import ru.radiationx.data.app.updater.remote.UpdateDataRootResponse

// TODO API2 check methods
interface DirectApi {

    @GET
    suspend fun checkUrl(@Url url: String)

    @GET
    suspend fun getUpdate(@Url url: String): UpdateDataRootResponse

    @GET
    suspend fun getApiConfig(@Url url: String): ApiConfigResponse

    @GET
    suspend fun getDonationConfig(@Url url: String): DonationInfoResponse

    @GET
    suspend fun getMenuConfig(@Url url: String): List<LinkMenuResponse>

    @GET
    suspend fun getAdsConfig(@Url url: String): AdsConfigDataResponse

    @GET
    suspend fun getFile(@Url url: String): Response<ResponseBody>

    @GET
    suspend fun getStringBody(@Url url: String): String

    @POST
    suspend fun getYooMoney(
        @Url url: String,
        @Body body: RequestBody
    ): Response<Unit>

}