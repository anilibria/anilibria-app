package ru.radiationx.anilibria.model

import ru.radiationx.anilibria.ui.fragments.other.OtherMenuItemState
import ru.radiationx.anilibria.ui.fragments.other.ProfileItemState
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.other.OtherMenuItem
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.entity.common.AuthState

fun ReleaseItem.toState(): ReleaseItemState {
    val title = if (series == null) {
        title.toString()
    } else {
        "$title ($series)"
    }
    return ReleaseItemState(
        id = id,
        title = title,
        description = description.orEmpty(),
        posterUrl = poster.orEmpty(),
        isNew = isNew
    )
}

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

fun ProfileItem.toState(): ProfileItemState {
    val hasAuth = authState == AuthState.AUTH
    val title = if (hasAuth) {
        nick
    } else {
        "Гость"
    }
    val subtitle = if (hasAuth) {
        null
    } else {
        "Авторизоваться"
    }
    val avatar = avatarUrl?.takeIf { it.isNotEmpty() } ?: "assets://res/alib_new_or_b.png"
    return ProfileItemState(
        id = id,
        hasAuth = hasAuth,
        title = title,
        subtitle = subtitle,
        avatar = avatar
    )
}

fun OtherMenuItem.toState() = OtherMenuItemState(
    id = id,
    title = title,
    icon = icon
)