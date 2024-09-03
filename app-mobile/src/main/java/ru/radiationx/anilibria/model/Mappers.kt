package ru.radiationx.anilibria.model

import ru.radiationx.anilibria.ui.fragments.other.OtherMenuItemState
import ru.radiationx.anilibria.ui.fragments.other.ProfileItemState
import ru.radiationx.data.apinext.models.SocialType
import ru.radiationx.data.apinext.models.User
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.other.OtherMenuItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.youtube.YoutubeItem

fun Release.toState(updates: Map<ReleaseId, ReleaseUpdate>): ReleaseItemState {
    val update = updates[id]
    val isNew = update
        ?.let { it.lastOpenTimestamp < freshAt || it.timestamp < freshAt }
        ?: false
    return ReleaseItemState(
        id = id,
        title = names.main,
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

fun User?.toState(): ProfileItemState {
    val title = this?.let {
        nickname ?: "Ник не указан"
    } ?: "Гость"
    val subtitle = if (this != null) {
        null
    } else {
        "Авторизоваться"
    }
    val avatar = this?.avatar
        ?.takeIf { it.isNotEmpty() }
        ?: "file:///android_asset/res/alib_new_or_b.png"
    return ProfileItemState(
        hasAuth = this != null,
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

fun SocialType.toState(): SocialAuthItemState {
    val title = when (this) {
        SocialType.VK -> "ВКонтакте"
        SocialType.GOOGLE -> "Google"
        SocialType.PATREON -> "Patreon"
        SocialType.DISCORD -> "Discord"
    }
    return SocialAuthItemState(
        type = this,
        title = title,
        iconRes = key.asDataIconRes(),
        colorRes = key.asDataColorRes()
    )
}

fun Release.toSuggestionState(query: String): SuggestionItemState {
    val itemTitle = names.main
    val matchRanges = try {
        Regex(query, RegexOption.IGNORE_CASE).findAll(itemTitle).map { it.range }.toList()
    } catch (ignore: Throwable) {
        emptyList()
    }

    return SuggestionItemState(
        id,
        itemTitle,
        poster.orEmpty(),
        matchRanges
    )
}