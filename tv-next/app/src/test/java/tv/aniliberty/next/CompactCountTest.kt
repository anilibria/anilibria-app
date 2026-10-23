package tv.aniliberty.next

import org.junit.Assert.assertEquals
import org.junit.Test

class CompactCountTest {
    @Test
    fun formatsRequestedFavoriteCountsWithoutLocaleCommaOrTrailingZero() {
        mapOf(
                0L to "0",
                1L to "1",
                25L to "25",
                38L to "38",
                999L to "999",
                1000L to "1к",
                1400L to "1.4к",
                12000L to "12к",
                25600L to "25.6к",
                1999L to "1.9к",
            )
            .forEach { (value, expected) -> assertEquals(expected, compactCount(value)) }
    }
}
