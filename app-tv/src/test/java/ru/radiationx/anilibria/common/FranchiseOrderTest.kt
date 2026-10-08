package ru.radiationx.anilibria.common

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.radiationx.data.entity.domain.release.Franchise
import ru.radiationx.data.entity.domain.release.FranchiseInfo
import ru.radiationx.data.entity.domain.release.FranchiseRelease
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId

class FranchiseOrderTest {
    private fun release(id: Int, ordinal: Int? = null) = FranchiseRelease(
        ReleaseId(id), listOf("Релиз $id"), ReleaseCode("release-$id"), ordinal,
    )

    private fun franchise(vararg releases: FranchiseRelease) =
        Franchise(FranchiseInfo("series", "Франшиза"), releases.toList())

    @Test
    fun `explicit ordinals sort the row and preserve gaps`() {
        val entries = franchiseOrder(listOf(franchise(release(3, 5), release(1, 1), release(2, 3))))
        assertEquals(listOf(ReleaseId(1), ReleaseId(2), ReleaseId(3)), entries.map { it.release.id })
        assertEquals(listOf(1, 3, 5), entries.map { it.ordinal })
    }

    @Test
    fun `older API without ordinals retains its order and duplicates appear once`() {
        val entries = franchiseOrder(listOf(franchise(release(2), release(1)), franchise(release(1))))
        assertEquals(listOf(ReleaseId(2), ReleaseId(1)), entries.map { it.release.id })
        assertEquals(listOf(1, 2), entries.map { it.ordinal })
    }

    @Test
    fun `only the matching generated list is removed and subsequent prose survives`() {
        val description = "Сюжет.\n\nПорядок просмотра франшизы \"Франшиза\":\n#1 Релиз 1 \n#3 Релиз 2\n\nПримечание автора."
        assertEquals("Сюжет.\n\n\nПримечание автора.", description.withoutFranchiseOrder(
            listOf(franchise(release(1, 1), release(2, 3))),
        ))
    }

    @Test
    fun `missing metadata and nonmatching lists keep the original text`() {
        val description = "Сюжет.\nПорядок просмотра франшизы \"Франшиза\":\n#1 Релиз 1\n#2 Другая версия"
        assertEquals(description, description.withoutFranchiseOrder(emptyList()))
        assertEquals(description, description.withoutFranchiseOrder(listOf(franchise(release(1), release(2)))))
        assertEquals(description, description.withoutFranchiseOrder(listOf(franchise(release(1)))))
    }
}
