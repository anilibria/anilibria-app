package ru.radiationx.data.app

import retrofit2.HttpException
import retrofit2.Response

fun <T> Response<T>.requireSuccess(): Response<T> {
    if (!isSuccessful) {
        throw HttpException(this)
    }
    return this
}