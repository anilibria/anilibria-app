package ru.radiationx.data.entity.response

/**
 * Created by radiationx on 04.12.2017.
 */
data class PaginatedResponse<out T>(
    val data: T,
    val page: Int?,
    val allPages: Int?,
    val perPage: Int?,
    val allItems: Int?
) {

    fun isEnd(): Boolean {
        return if (page != null && allPages != null) {
            page >= allPages
        } else {
            false
        }
    }
}
