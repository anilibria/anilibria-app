package anilibria.api.profile

import anilibria.api.profile.models.ProfileResponse
import retrofit2.http.GET

interface ProfileApi {

    @GET("accounts/users/me/profile")
    suspend fun getProfile(): ProfileResponse
}