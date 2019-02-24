package ru.radiationx.anilibria.model.data.remote

import io.reactivex.Single
import io.reactivex.SingleTransformer
import org.json.JSONObject
import ru.radiationx.anilibria.extension.nullGet
import ru.radiationx.anilibria.extension.nullString

@Suppress("UNCHECKED_CAST")
open class ApiResponse<T>(
        jsonString: String
) {
    private var status: Boolean? = null
    private var data: T? = null
    private var error: ApiError? = null

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

    open fun handleError(): Single<ApiResponse<T>> {
        return when {
            status == true && data != null -> Single.just(this)
            error != null -> Single.error(error)
            else -> Single.error(Exception("Wrong response"))
        }
    }

    companion object {
        fun <T> fetchResult(): SingleTransformer<String, T> = SingleTransformer {
            it.flatMap { t -> ApiResponse<T>(t).handleError() }.map { t -> t.data }
        }
    }
}
