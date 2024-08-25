package anilibria.api.profile

import anilibria.api.profile.models.ProfileResponse
import de.jensklingenberg.ktorfit.http.GET

interface ProfileApi {

    @GET("/accounts/users/me/profile")
    suspend fun getProfile(): ProfileResponse
}