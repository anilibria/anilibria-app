package anilibria.api.timecodes

import anilibria.api.timecodes.models.TimeCodeDeleteRequest
import anilibria.api.timecodes.models.TimeCodeNetwork
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface TimeCodesApi {

    @GET("accounts/users/me/views/timecodes")
    suspend fun get(): List<TimeCodeNetwork>

    @POST("accounts/users/me/views/timecodes")
    suspend fun update(@Body body: List<TimeCodeNetwork>)

    @HTTP(method = "DELETE", path = "accounts/users/me/views/timecodes", hasBody = true)
    suspend fun delete(@Body body: List<TimeCodeDeleteRequest>)
}