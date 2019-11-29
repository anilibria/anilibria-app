package ru.radiationx.anilibria.model.system

import okhttp3.Response
import java.lang.RuntimeException

class HttpException(
        val code: Int,
        override val message: String,
        val response: Response
) : RuntimeException()