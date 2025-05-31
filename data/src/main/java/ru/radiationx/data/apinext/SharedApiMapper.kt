package ru.radiationx.data.apinext

import anilibria.api.shared.ReleaseIdNetwork
import ru.radiationx.data.apinext.models.RelativeUrl
import ru.radiationx.data.apinext.models.filters.FormItem
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.ReleaseIdentifier
import java.text.SimpleDateFormat
import java.util.Date

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
fun String.apiDateToDate(): Date {
    return dateFormat.parse(this)
}

fun Iterable<FormItem>.toListRequest(): List<String> {
    return map { it.toQuery() }
}

fun Iterable<FormItem>.toStringRequest(): String {
    return toListRequest().joinToString(",")
}

fun FormItem.toQuery(): String {
    return when (this) {
        is FormItem.Genre -> id.id.toString()
        is FormItem.Value -> value
        is FormItem.Year -> year.toString()
    }
}

fun ReleaseId.toNetwork(): ReleaseIdNetwork {
    return ReleaseIdNetwork(
        releaseId = id
    )
}

fun String.toRelativeUrl(): RelativeUrl {
    return RelativeUrl(
        url = this
    )
}

fun ReleaseIdentifier.toRequestIdentifier(): String {
    return when (this) {
        is ReleaseId -> id.toString()
        is ReleaseCode -> code
    }
}
