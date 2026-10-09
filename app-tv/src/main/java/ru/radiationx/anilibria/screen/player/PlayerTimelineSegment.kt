package ru.radiationx.anilibria.screen.player

import ru.radiationx.data.entity.domain.release.PlayerSkips

internal data class PlayerTimelineSegment(
    val type: Type,
    val start: Long,
    val end: Long,
) {
    enum class Type { OPENING, ENDING }
}

internal fun PlayerSkips?.timelineSegments(duration: Long): List<PlayerTimelineSegment> {
    if (this == null || duration <= 0) return emptyList()
    fun segment(skip: PlayerSkips.Skip?, type: PlayerTimelineSegment.Type): PlayerTimelineSegment? {
        if (skip == null || skip.start < 0 || skip.end <= skip.start || skip.start >= duration) {
            return null
        }
        return PlayerTimelineSegment(type, skip.start, skip.end.coerceAtMost(duration))
    }
    return listOfNotNull(
        segment(opening, PlayerTimelineSegment.Type.OPENING),
        segment(ending, PlayerTimelineSegment.Type.ENDING),
    ).sortedBy { it.start }
}
