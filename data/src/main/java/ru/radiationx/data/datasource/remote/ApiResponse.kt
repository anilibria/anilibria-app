package ru.radiationx.data.datasource.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.entity.response.PaginatedResponse
import java.lang.reflect.Type

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
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

suspend fun <T> String.fetchResponse(moshi: Moshi, dataType: Type): T {
    return withContext(Dispatchers.Default) {
        val adapter = moshi.adapter<T>(dataType)
        val response = adapter.fromJson(this@fetchResponse)
        requireNotNull(response) {
            "Can't parse response, result is null"
        }
        response
    }
}

suspend inline fun <reified T> String.fetchResponse(moshi: Moshi): T {
    return fetchResponse(moshi, T::class.java)
}

suspend fun <T> String.fetchApiResponse(moshi: Moshi, dataType: Type): T {
    return withContext(Dispatchers.Default) {
        val responseType = Types.newParameterizedType(ApiResponse::class.java, dataType)
        val adapter = moshi.adapter<ApiResponse<T>>(responseType)
        val apiResponse = adapter.fromJson(this@fetchApiResponse)
        requireNotNull(apiResponse) {
            "Can't parse response, result is null"
        }
        apiResponse.fetch()
    }
}

suspend inline fun <reified T> String.fetchApiResponse(moshi: Moshi): T {
    return fetchApiResponse(moshi, T::class.java)
}

suspend inline fun <reified T> String.fetchListApiResponse(moshi: Moshi): List<T> {
    val dataType = Types.newParameterizedType(List::class.java, T::class.java)
    return fetchApiResponse(moshi, dataType)
}

suspend inline fun <reified T> String.fetchPaginatedApiResponse(moshi: Moshi): PaginatedResponse<T> {
    val dataType = Types.newParameterizedType(PaginatedResponse::class.java, T::class.java)
    return fetchApiResponse(moshi, dataType)
}

suspend fun String.fetchEmptyApiResponse(moshi: Moshi) {
    fetchApiResponse<Any>(moshi, Any::class.java)
}
