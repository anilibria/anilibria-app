package ru.radiationx.data.entity.mapper

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.radiationx.data.entity.response.release.FranchiseReleaseResponse

class FranchiseMapperTest {
    private val adapter = Moshi.Builder().build().adapter(FranchiseReleaseResponse::class.java)
    private val fields = "\"id\":1,\"name\":\"First\",\"ename\":\"First\",\"alias\":\"first\""

    @Test
    fun `API ordinal reaches the domain model`() {
        assertEquals(5, adapter.fromJson("{$fields,\"ordinal\":5}")!!.toDomain().ordinal)
    }

    @Test
    fun `older payload without ordinal is supported`() {
        assertNull(adapter.fromJson("{$fields}")!!.toDomain().ordinal)
    }
}
