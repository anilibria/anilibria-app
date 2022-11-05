package ru.radiationx.data.datasource.remote

import com.squareup.moshi.*
import org.json.JSONObject
import ru.radiationx.data.entity.response.PaginatedResponse
import ru.radiationx.shared.ktx.android.nullGet
import ru.radiationx.shared.ktx.android.nullString
import java.lang.reflect.Type

@Deprecated("use moshi response")
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

@Deprecated("use moshi response")
suspend fun <T> String.fetchResult(): T {
    val apiResponse = ApiResponse<T>(this)
    apiResponse.handleError()
    return requireNotNull(apiResponse.data)
}

fun <T> String.fetchResponse(moshi: Moshi, dataType: Type): T {
    val adapter = moshi.adapter<T>(dataType)
    val response = adapter.fromJson(this)
    requireNotNull(response) {
        "Can't parse response, result is null"
    }
    return response
}

inline fun <reified T> String.fetchResponse(moshi: Moshi): T {
    return fetchResponse(moshi, T::class.java)
}

fun <T> String.fetchApiResponse(moshi: Moshi, dataType: Type): T {
    val responseType = Types.newParameterizedType(MoshiApiResponse::class.java, dataType)
    val adapter = moshi.adapter<MoshiApiResponse<T>>(responseType)
    val apiResponse = adapter.fromJson(this)
    requireNotNull(apiResponse) {
        "Can't parse response, result is null"
    }
    return apiResponse.fetch()
}

inline fun <reified T> String.fetchApiResponse(moshi: Moshi): T {
    return fetchApiResponse(moshi, T::class.java)
}

inline fun <reified T> String.fetchListApiResponse(moshi: Moshi): List<T> {
    val dataType = Types.newParameterizedType(List::class.java, T::class.java)
    return fetchApiResponse(moshi, dataType)
}

inline fun <reified T> String.fetchPaginatedApiResponse(moshi: Moshi): PaginatedResponse<T> {
    val dataType = Types.newParameterizedType(PaginatedResponse::class.java, T::class.java)
    return fetchApiResponse(moshi, dataType)
}

fun String.fetchEmptyApiResponse(moshi: Moshi) {
    fetchApiResponse<Any>(moshi, Any::class.java)
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
