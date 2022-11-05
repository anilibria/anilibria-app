package ru.radiationx.data.entity.app

/**
 * Created by radiationx on 04.12.2017.
 */
data class Paginated<T>(
    val data: List<T>,
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
