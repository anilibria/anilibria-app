package ru.radiationx.data.entity.response.search

import com.squareup.moshi.Moshi
import org.junit.Assert.*
import org.junit.Test

class SuggestionResponseTest {
    private val adapter = Moshi.Builder().build().adapter(SuggestionResponse::class.java)

    @Test
    fun `search response retains block and favorite information`() {
        val response = requireNotNull(adapter.fromJson("""
            {"id":123,"code":"release","names":["Название"],"poster":"poster.jpg",
             "blockedInfo":{"blocked":true,"reason":"Причина"},
             "favorite":{"rating":10,"added":true}}
        """.trimIndent()))
        assertTrue(requireNotNull(response.blockedInfo).isBlocked)
        assertEquals("Причина", response.blockedInfo.reason)
        assertTrue(requireNotNull(response.favorite).isAdded)
    }

    @Test
    fun `older search responses without status remain readable`() {
        val response = requireNotNull(adapter.fromJson("""
            {"id":123,"code":"release","names":["Название"],"poster":null}
        """.trimIndent()))
        assertNull(response.blockedInfo)
        assertNull(response.favorite)
    }
}
