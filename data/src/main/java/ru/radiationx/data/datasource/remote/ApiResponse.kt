package ru.radiationx.data.datasource.remote

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
