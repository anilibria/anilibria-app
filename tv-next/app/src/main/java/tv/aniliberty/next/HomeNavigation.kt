package tv.aniliberty.next

/** Home navigation skips labels and never crosses a horizontal shelf boundary implicitly. */
fun homeFocusDestination(rows: List<Row>, from: Int, direction: Int): Int? {
    val indices = if (direction < 0) (from - 1 downTo 0) else (from + 1 until rows.size)
    return indices.firstOrNull {
        rows[it].kind in setOf(Kind.SHELF, Kind.DAYS, Kind.ACTION) ||
            (rows[it].kind == Kind.HEADING && rows[it].click != null)
    }
}

/** Keep the heading and whole shelf visible; don't move a block that's already visible. */
fun homeScrollDelta(
    headerTop: Int,
    contentBottom: Int,
    viewportTop: Int,
    viewportBottom: Int,
): Int =
    when {
        headerTop < viewportTop -> headerTop - viewportTop
        contentBottom > viewportBottom ->
            minOf(headerTop - viewportTop, contentBottom - viewportBottom)
        else -> 0
    }
