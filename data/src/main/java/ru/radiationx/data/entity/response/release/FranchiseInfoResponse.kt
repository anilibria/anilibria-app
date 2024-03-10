package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FranchiseResponse(
    @Json(name = "franchise") val info: FranchiseInfoResponse,
    @Json(name = "releases") val releases: List<FranchiseReleaseResponse>,
)

@JsonClass(generateAdapter = true)
data class FranchiseInfoResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
)

@JsonClass(generateAdapter = true)
data class FranchiseReleaseResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "ename") val ename: String,
    @Json(name = "aname") val aname: String?,
    @Json(name = "alias") val alias: String,
)