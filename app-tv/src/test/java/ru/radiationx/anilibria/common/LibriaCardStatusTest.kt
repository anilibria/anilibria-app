package ru.radiationx.anilibria.common

import org.junit.Assert.*
import org.junit.Test
import ru.radiationx.data.entity.domain.release.BlockedInfo
import ru.radiationx.data.entity.domain.release.FavoriteInfo
import ru.radiationx.data.entity.domain.types.ReleaseId

class LibriaCardStatusTest {
    private val card = LibriaCard(
        "Название", "2026 • Комедия • Серии: 1-12", "poster.jpg",
        LibriaCard.Type.Release(ReleaseId(123)),
    )

    @Test
    fun `blocked release replaces metadata with trimmed reason and keeps title`() {
        val result = card.withReleaseStatus(BlockedInfo(true, "  По требованию правообладателя  "), FavoriteInfo(0, false))
        assertEquals(card.title, result.title)
        assertEquals("По требованию правообладателя", result.description)
        assertTrue(result.isBlocked)
    }

    @Test
    fun `missing blocked reason uses fallback`() {
        listOf(null, "", " \n ").forEach { reason ->
            val result = card.withReleaseStatus(BlockedInfo(true, reason), FavoriteInfo(0, false))
            assertEquals("Релиз недоступен", result.description)
        }
    }

    @Test
    fun `block reason takes priority over continue watching subtitle`() {
        val continueCard = card.copy(description = "Вы остановились на 2 серии")
        val result = continueCard.withReleaseStatus(BlockedInfo(true, "Причина"), FavoriteInfo(10, true))
        assertEquals("Причина", result.description)
    }

    @Test
    fun `unblocked release keeps metadata even if reason is present`() {
        val result = card.withReleaseStatus(BlockedInfo(false, "Старая причина"), FavoriteInfo(0, false))
        assertEquals(card.description, result.description)
        assertFalse(result.isBlocked)
    }

    @Test
    fun `favorite and blocked states are independent`() {
        val result = card.withReleaseStatus(BlockedInfo(true, "Причина"), FavoriteInfo(10, true))
        assertTrue(result.isBlocked)
        assertTrue(result.isFavorite)
        assertTrue(result.showFavoriteBadge)
    }

    @Test
    fun `favorite rows suppress badge while preserving blocked state`() {
        val result = card.withReleaseStatus(BlockedInfo(true, "Причина"), FavoriteInfo(10, true), false)
        assertTrue(result.isFavorite)
        assertFalse(result.showFavoriteBadge)
        assertTrue(result.isBlocked)
        assertEquals("Причина", result.description)
    }

    @Test
    fun `ordinary release resets status when replacing blocked favorite card`() {
        val blocked = card.withReleaseStatus(BlockedInfo(true, "Причина"), FavoriteInfo(10, true))
        val ordinary = card.withReleaseStatus(BlockedInfo(false, null), FavoriteInfo(0, false))
        assertEquals(blocked.getId(), ordinary.getId())
        assertFalse(ordinary.isBlocked)
        assertFalse(ordinary.isFavorite)
        assertEquals(card.description, ordinary.description)
    }
}
