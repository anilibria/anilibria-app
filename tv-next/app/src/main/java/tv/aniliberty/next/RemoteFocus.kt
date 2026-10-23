package tv.aniliberty.next

import android.view.View

/** Follow the actual span layout, preserving the column across short rows. */
internal fun remoteGridTarget(
    spans: List<Int>,
    focusable: List<Boolean>,
    count: Int,
    position: Int,
    direction: Int,
): Int? {
    if (position !in spans.indices || count < 1) return null
    data class Cell(val index: Int, val line: Int, val start: Int, val end: Int)
    var line = 0
    var offset = 0
    val cells =
        spans.mapIndexed { index, size ->
            val span = size.coerceIn(1, count)
            if (offset + span > count) {
                line++
                offset = 0
            }
            val cell = Cell(index, line, offset, offset + span)
            offset += span
            if (offset == count) {
                line++
                offset = 0
            }
            cell
        }
    val from = cells[position]
    val candidates =
        cells.filter { focusable.getOrElse(it.index) { false } && it.index != position }
    return when (direction) {
        View.FOCUS_LEFT ->
            candidates
                .filter { it.line == from.line && it.end <= from.start }
                .maxByOrNull { it.end }
        View.FOCUS_RIGHT ->
            candidates
                .filter { it.line == from.line && it.start >= from.end }
                .minByOrNull { it.start }
        View.FOCUS_UP,
        View.FOCUS_DOWN -> {
            val forward = direction == View.FOCUS_DOWN
            val eligible =
                candidates.filter { if (forward) it.line > from.line else it.line < from.line }
            val targetLine =
                if (forward) eligible.minOfOrNull { it.line } else eligible.maxOfOrNull { it.line }
            eligible
                .filter { it.line == targetLine }
                .minByOrNull { kotlin.math.abs((it.start + it.end) - (from.start + from.end)) }
        }
        else -> null
    }?.index
}
