package ru.radiationx.data.apinext

import ru.radiationx.data.apinext.models.filters.FormItem
import java.text.SimpleDateFormat
import java.util.Date

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
fun String.apiDateToDate(): Date {
    return dateFormat.parse(this)
}

fun Iterable<FormItem>.toListQuery(): String {
    return joinToString(",") { filterItem ->
        filterItem.toQuery()
    }
}

fun FormItem.toQuery(): String {
    return when (this) {
        is FormItem.Genre -> id.id.toString()
        is FormItem.Value -> value
        is FormItem.Year -> year.toString()
    }
}
