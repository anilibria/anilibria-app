package ru.radiationx.data.system

import okhttp3.Response

class HttpException(
        val code: Int,
        override val message: String,
        val response: Response
) : RuntimeException()