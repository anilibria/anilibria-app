package ru.radiationx.data.apinext

import ru.radiationx.data.apinext.models.filters.FilterItem
import java.text.SimpleDateFormat
import java.util.Date

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
fun String.apiDateToDate(): Date {
    return dateFormat.parse(this)
}

fun List<FilterItem>.toQuery(): String {
    return joinToString(",") { filterItem ->
        when (filterItem) {
            is FilterItem.Genre -> filterItem.id.toString()
            is FilterItem.Value -> filterItem.value
            is FilterItem.Year -> filterItem.year.toString()
        }
    }
}
