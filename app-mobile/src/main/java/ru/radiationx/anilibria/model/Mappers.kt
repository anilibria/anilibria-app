package ru.radiationx.anilibria.model

import ru.radiationx.anilibria.ui.fragments.other.OtherMenuItemState
import ru.radiationx.anilibria.ui.fragments.other.ProfileItemState
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.auth.SocialAuth
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.other.OtherMenuItem
import ru.radiationx.data.entity.domain.other.ProfileItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.search.SuggestionItem
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.youtube.YoutubeItem

fun Release.toState(updates: Map<ReleaseId, ReleaseUpdate>): ReleaseItemState {
    val title = if (series == null) {
        title.toString()
    } else {
        "$title ($series)"
    }
    val update = updates[id]
    val isNew = update
        ?.let { it.lastOpenTimestamp < torrentUpdate || it.timestamp < torrentUpdate }
        ?: false
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

fun FeedItem.toState(updates: Map<ReleaseId, ReleaseUpdate>) = FeedItemState(
    id = id,
    release = release?.toState(updates),
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
    iconRes = icon
)

fun SocialAuth.toState(): SocialAuthItemState = SocialAuthItemState(
    key = key,
    title = title,
    iconRes = key.asDataIconRes(),
    colorRes = key.asDataColorRes()
)

fun SuggestionItem.toState(query: String): SuggestionItemState {
    val itemTitle = names.firstOrNull().orEmpty()
    val matchRanges = try {
        Regex(query, RegexOption.IGNORE_CASE).findAll(itemTitle).map { it.range }.toList()
    } catch (ignore: Throwable) {
        emptyList<IntRange>()
    }

    return SuggestionItemState(
        id,
        itemTitle,
        poster.orEmpty(),
        matchRanges
    )
}