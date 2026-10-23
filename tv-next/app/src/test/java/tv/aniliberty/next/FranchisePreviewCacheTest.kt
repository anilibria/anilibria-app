package tv.aniliberty.next

import org.junit.Assert.*
import org.junit.Test

class FranchisePreviewCacheTest {
    @Test
    fun evictsLeastRecentlyUsedEntriesAndExpiresTitles() {
        var time = 0L
        val cache = FranchisePreviewCache(2, 100, 1000) { time }
        cache["a"] = listOf("A")
        cache["b"] = listOf("B")
        assertEquals(listOf("A"), cache["a"])
        cache["c"] = listOf("C")
        assertNull(cache["b"])
        assertEquals(2, cache.size)
        time = 1000
        assertNull(cache["a"])
    }

    @Test
    fun characterBudgetCannotBeExceededEvenByOneLargeFranchise() {
        val cache = FranchisePreviewCache(maxCharacters = 10)
        cache["a"] = listOf("123456")
        cache["b"] = listOf("7890")
        assertNull(cache["a"])
        assertTrue(cache.characters <= 10)
        cache["huge"] = listOf("x".repeat(100))
        assertNull(cache["huge"])
        assertTrue(cache.characters <= 10)
    }
}
