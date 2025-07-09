package anilibria.api.views

import anilibria.api.shared.PaginationResponse
import anilibria.api.views.models.TimeCodeDeleteRequest
import anilibria.api.views.models.TimeCodeNetwork
import anilibria.api.views.models.ViewHistoryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface ViewsApi {

    @GET("accounts/users/me/views/history")
    suspend fun getHistory(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): PaginationResponse<ViewHistoryResponse>

    /*
    * [0] - release_episode_id: String
    * [1] - time: Double
    * [2] - is_watched: Boolean
    * */
    @GET("accounts/users/me/views/timecodes")
    suspend fun getTimeCodes(
        @Query("since") since: String?
    ): List<List<Any>>

    /*
    * [0] - release_episode_id: String
    * [1] - time: Double
    * [2] - is_watched: Boolean
    * */
    @POST("accounts/users/me/views/timecodes")
    suspend fun updateTimeCodes(@Body body: List<TimeCodeNetwork>)

    /*
    * [0] - release_episode_id: String
    * [1] - time: Double
    * [2] - is_watched: Boolean
    * */
    @HTTP(method = "DELETE", path = "accounts/users/me/views/timecodes", hasBody = true)
    suspend fun deleteTimeCodes(@Body body: List<TimeCodeDeleteRequest>)
}