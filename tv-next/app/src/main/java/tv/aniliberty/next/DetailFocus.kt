package tv.aniliberty.next

/** Rows in a title are grouped by kind; horizontal edges never wrap into another row. */
internal fun detailGridTarget(rows: List<Row>, position: Int, direction: Int): Int? {
    val kind = rows.getOrNull(position)?.kind ?: return null
    val columns =
        when (kind) {
            Kind.EPISODE -> 3
            Kind.POSTER -> 5
            else -> 1
        }
    val first =
        (position downTo 0).firstOrNull {
            rows[it].kind == kind && (it == 0 || rows[it - 1].kind != kind)
        } ?: position
    val last =
        (position until rows.size).firstOrNull { rows[it].kind != kind }?.minus(1) ?: rows.lastIndex
    val column = (position - first) % columns
    return when (direction) {
        android.view.View.FOCUS_LEFT -> (position - 1).takeIf { column > 0 }
        android.view.View.FOCUS_RIGHT ->
            (position + 1).takeIf { column < columns - 1 && it <= last }
        android.view.View.FOCUS_UP -> (position - columns).takeIf { it >= first }
        android.view.View.FOCUS_DOWN ->
            if (position + columns <= last) position + columns
            else {
                val next = first + ((position - first) / columns + 1) * columns
                if (next <= last) last else null
            }
        else -> null
    }
}

internal data class DetailBookmark(val row: String, val child: String? = null)
