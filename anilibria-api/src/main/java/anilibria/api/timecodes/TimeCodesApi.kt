package anilibria.api.timecodes

import anilibria.api.timecodes.models.TimeCodeDeleteRequest
import anilibria.api.timecodes.models.TimeCodeNetwork
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface TimeCodesApi {

    /*
    * [0] - release_episode_id: String
    * [1] - time: Double
    * [2] - is_watched: Boolean
    * */
    @GET("accounts/users/me/views/timecodes")
    suspend fun get(): List<List<Any>>

    @POST("accounts/users/me/views/timecodes")
    suspend fun update(@Body body: List<TimeCodeNetwork>)

    @HTTP(method = "DELETE", path = "accounts/users/me/views/timecodes", hasBody = true)
    suspend fun delete(@Body body: List<TimeCodeDeleteRequest>)
}