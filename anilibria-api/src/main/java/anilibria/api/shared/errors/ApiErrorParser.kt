package anilibria.api.shared.errors

import com.squareup.moshi.Moshi
import retrofit2.HttpException
import javax.inject.Inject


class ApiErrorParser @Inject constructor(
    private val moshi: Moshi
) {

    fun getCommonError(exception: HttpException): CommonApiError? {
        val response = exception.response() ?: return null
        if (response.code() == 422) return null
        val errorBody = response.errorBody() ?: return null
        return try {
            val adapter = moshi.adapter(CommonApiError::class.java)
            adapter.fromJson(errorBody.source())
        } catch (ignore: Exception) {
            null
        }
    }

    fun getValidationError(exception: HttpException): ValidationApiError? {
        val response = exception.response() ?: return null
        if (response.code() != 422) return null
        val errorBody = response.errorBody() ?: return null
        return try {
            val adapter = moshi.adapter(ValidationApiError::class.java)
            adapter.fromJson(errorBody.source())
        } catch (ignore: Exception) {
            null
        }
    }
}

