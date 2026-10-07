package ru.radiationx.anilibria.screen.player.episodes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId

class EpisodeProgressDescriptionTest {
    private val id = EpisodeId("1", ReleaseId(1))

    @Test fun `unfinished episode still shows saved position`() {
        assertEquals("Остановлена на 01:30", EpisodeAccess(id, 90_000L, false, 1L).progressDescription())
    }

    @Test fun `manual viewed mark without progress has no stopped position`() {
        assertNull(EpisodeAccess(id, 0L, true, 0L).progressDescription())
        assertNull((null as EpisodeAccess?).progressDescription())
    }
}
