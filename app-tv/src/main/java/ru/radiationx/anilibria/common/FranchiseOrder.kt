package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.domain.release.Franchise
import ru.radiationx.data.entity.domain.release.FranchiseRelease

internal data class FranchiseOrderEntry(val release: FranchiseRelease, val ordinal: Int)

internal fun franchiseOrder(franchises: List<Franchise>): List<FranchiseOrderEntry> =
    franchises.flatMap { franchise ->
        franchise.releases.mapIndexed { index, release ->
            FranchiseOrderEntry(release, release.ordinal?.takeIf { it > 0 } ?: (index + 1))
        }.sortedBy { it.ordinal }
    }.distinctBy { it.release.id }

// Only remove the duplicate generated list; keep prose and unrecognised order notes.
internal fun String.withoutFranchiseOrder(franchises: List<Franchise>): String {
    if (franchises.flatMap { it.releases }.distinctBy { it.id }.size < 2) return this
    val lines = split('\n').toMutableList()
    franchises.forEach { franchise ->
        val entries = franchiseOrder(listOf(franchise))
        if (entries.isEmpty()) return@forEach
        val header = "Порядок просмотра франшизы \"${franchise.info.name.trim()}\":"
        val start = lines.indexOfFirst { it.trim() == header }
        if (start < 0 || start + entries.size >= lines.size) return@forEach
        val matches = entries.withIndex().all { (index, entry) ->
            val expected = "#${entry.ordinal} ${entry.release.names.firstOrNull()?.trim().orEmpty()}"
            lines[start + index + 1].trim() == expected
        }
        if (matches) repeat(entries.size + 1) { lines.removeAt(start) }
    }
    return lines.joinToString("\n").trim()
}
