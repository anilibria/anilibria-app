package anilibria.apigen.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import com.squareup.moshi.Json

import anilibria.apigen.models.Modelsadsvastsv1vast

interface VastsApi {
    /**
     * GET ads/vasts/chain
     * VAST XML с цепочкой реклам
     * Возвращает XML страницу со всеми доступными для использования VAST кампаниями
     * Responses:
     *  - 200: XML VAST
     *
     * @return [Unit]
     */
    @GET("ads/vasts/chain")
    suspend fun adsVastsChainGet(): Response<Unit>

    /**
     * GET ads/vasts
     * Список возможных VAST реклам
     * Возвращает список со всеми доступными для использования VAST кампаниями
     * Responses:
     *  - 200: Vasts
     *
     * @return [kotlin.collections.List<Modelsadsvastsv1vast>]
     */
    @GET("ads/vasts")
    suspend fun adsVastsGet(): Response<kotlin.collections.List<Modelsadsvastsv1vast>>

}
