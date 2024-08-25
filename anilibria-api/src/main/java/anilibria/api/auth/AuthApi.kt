package anilibria.api.auth

import anilibria.api.auth.models.EmptyTokenResponse
import anilibria.api.auth.models.LoginRequest
import anilibria.api.auth.models.LoginSocialResponse
import anilibria.api.auth.models.OtpAcceptRequest
import anilibria.api.auth.models.OtpGetRequest
import anilibria.api.auth.models.OtpGetResponse
import anilibria.api.auth.models.OtpLoginRequest
import anilibria.api.auth.models.PasswordForgetRequest
import anilibria.api.auth.models.PasswordResetRequest
import anilibria.api.auth.models.TokenResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface AuthApi {

    @POST("/accounts/otp/get")
    suspend fun getOtp(@Body body: OtpGetRequest): OtpGetResponse

    @POST("/accounts/otp/accept")
    suspend fun acceptOtp(@Body body: OtpAcceptRequest)

    @POST("/accounts/otp/login")
    suspend fun loginOtp(@Body body: OtpLoginRequest): TokenResponse


    @POST("/accounts/users/auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("/accounts/users/auth/logout")
    suspend fun logout(): EmptyTokenResponse


    @GET("/accounts/users/auth/social/{provider}/login")
    suspend fun loginSocial(@Path("provider") provider: String): LoginSocialResponse

    @GET("/accounts/users/auth/social/authenticate")
    suspend fun authenticateSocial(@Query("state") state: String): TokenResponse

    @POST("/accounts/users/auth/password/forget")
    suspend fun passwordForget(@Body body: PasswordForgetRequest)

    @POST("/accounts/users/auth/password/reset")
    suspend fun passwordReset(@Body body: PasswordResetRequest)
}