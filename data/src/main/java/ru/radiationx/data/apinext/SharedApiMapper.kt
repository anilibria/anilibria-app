package ru.radiationx.data.apinext

import ru.radiationx.data.apinext.models.filters.FilterItem
import java.text.SimpleDateFormat
import java.util.Date

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
fun String.apiDateToDate(): Date {
    return dateFormat.parse(this)
}

fun List<FilterItem>.toListQuery(): String {
    return joinToString(",") { filterItem ->
        filterItem.toQuery()
    }
}

fun FilterItem.toQuery(): String {
    return when (this) {
        is FilterItem.Genre -> id.toString()
        is FilterItem.Value -> value
        is FilterItem.Year -> year.toString()
    }
}
