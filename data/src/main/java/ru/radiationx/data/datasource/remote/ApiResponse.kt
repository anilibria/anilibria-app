package ru.radiationx.data.datasource.remote

import com.squareup.moshi.*
import org.json.JSONObject
import ru.radiationx.shared.ktx.android.nullGet
import ru.radiationx.shared.ktx.android.nullString

@Suppress("UNCHECKED_CAST")
open class ApiResponse<T>(
    jsonString: String
) {
    val status: Boolean?
    val data: T?
    val error: ApiError?

    init {
        val jsonObject = JSONObject(jsonString)
        status = jsonObject.getBoolean("status")
        data = jsonObject.nullGet("data") as T?
        error = (jsonObject.nullGet("error") as JSONObject?)?.let { jsonError ->
            ApiError(
                jsonError.optInt("code"),
                jsonError.nullString("message"),
                jsonError.nullString("description")
            )
        }
    }

    open suspend fun handleError(): ApiResponse<T> = when {
        status == true && data != null -> this
        error != null -> throw error
        else -> throw Exception("Wrong response")
    }
}

suspend fun <T> String.fetchResult(): T {
    val apiResponse = ApiResponse<T>(this)
    apiResponse.handleError()
    return requireNotNull(apiResponse.data)
}


inline fun <reified T, reified R : MoshiApiResponse<T>> createAdapter(moshi: Moshi): JsonAdapter<R> {
    return moshi.adapter(R::class.java)
}

inline fun <reified T> String.fetchApiResponse(moshi: Moshi): T {
    val type = Types.newParameterizedType(MoshiApiResponse::class.java, T::class.java)
    val adapter = moshi.adapter<MoshiApiResponse<T>>(type)
    val apiResponse = adapter.fromJson(this)
    requireNotNull(apiResponse) {
        "Can't parse response, result is null"
    }
    return apiResponse.fetch()
}

@JsonClass(generateAdapter = true)
data class MoshiApiResponse<T>(
    @Json(name = "status") val status: Boolean?,
    @Json(name = "data") val data: T?,
    @Json(name = "error") val error: ApiErrorResponse?
) {

    fun fetch(): T = when {
        status == true && data != null -> data
        error != null -> throw ApiError(error.code, error.message, error.description)
        else -> throw Exception("Wrong response")
    }
}
