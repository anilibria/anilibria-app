package anilibria.api.timecodes

import anilibria.api.timecodes.models.TimeCodeDeleteRequest
import anilibria.api.timecodes.models.TimeCodeNetwork
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST

interface TimeCodesApi {

    @GET("/accounts/users/me/views/timecodes")
    suspend fun get(): List<TimeCodeNetwork>

    @POST("/accounts/users/me/views/timecodes")
    suspend fun update(@Body body: List<TimeCodeNetwork>)

    @DELETE("/accounts/users/me/views/timecodes")
    suspend fun delete(@Body body: List<TimeCodeDeleteRequest>): Unit
}