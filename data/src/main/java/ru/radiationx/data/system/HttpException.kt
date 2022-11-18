package ru.radiationx.data.system

import okhttp3.Response

data class HttpException(
    val code: Int,
    override val message: String,
    val response: Response
) : RuntimeException()