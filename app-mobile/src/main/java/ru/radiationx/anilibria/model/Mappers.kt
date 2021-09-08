package ru.radiationx.anilibria.model

import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem

fun ReleaseItem.toState() = ReleaseItemState(
    id = id,
    title = title.orEmpty(),
    description = description.orEmpty(),
    posterUrl = poster.orEmpty(),
    isNew = isNew
)

fun YoutubeItem.toState() = YoutubeItemState(
    id = id,
    title = title.orEmpty(),
    image = image.orEmpty(),
    views = views.toString(),
    comments = comments.toString()
)

fun FeedItem.toState() = FeedItemState(
    release = release?.toState(),
    youtube = youtube?.toState()
)

fun ScheduleItem.toState() = ScheduleItemState(
    releaseId = releaseItem.id,
    posterUrl = releaseItem.poster.orEmpty(),
    isCompleted = completed
)