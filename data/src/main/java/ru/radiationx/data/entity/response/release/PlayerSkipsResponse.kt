package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class PlayerSkipsResponse(
    @Json(name = "opening") val opening: List<Int>,
    @Json(name = "ending") val ending: List<Int>
) : Serializable