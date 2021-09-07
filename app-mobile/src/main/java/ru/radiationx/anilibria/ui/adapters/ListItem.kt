package ru.radiationx.anilibria.ui.adapters

import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.other.OtherMenuItem
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.TorrentItem
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem

sealed class ListItem(val idData: Any?) {

    open fun getItemId(): Long {
        return generateIdentifier(this::class.java, idData)
    }

    open fun getItemHash(): Int {
        return hashCode()
    }

    open fun getPayloadBy(oldItem: ListItem): Any? {
        return null
    }

    private fun generateIdentifier(vararg obj: Any?): Long {
        var identifier = 0L
        obj.forEach {
            if (it != null) {
                identifier = identifier * 31 + it.hashCode()
            }
        }
        return identifier
    }
}

/* Other screen*/

data class ProfileListItem(val profileItem: ProfileItem) : ListItem(profileItem.id)
data class MenuListItem(val menuItem: OtherMenuItem) : ListItem(menuItem.id)
data class DividerShadowListItem(val id: Any) : ListItem(id)


/* Common */

data class LoadMoreListItem(val id: Any) : ListItem(null)
data class CommentRouteListItem(val id: Any) : ListItem(null)
data class BottomTabListItem(
    val item: MainActivity.Tab,
    var selected: Boolean = false
) : ListItem(item.title)

data class PlaceholderListItem(
    val icRes: Int,
    val titleRes: Int,
    val descRes: Int
) : ListItem(titleRes)

/* Releases list screen */

data class ReleaseListItem(val item: ReleaseItem) : ListItem(item.id)


/* Release detail screen */

data class ReleaseEpisodeListItem(
    val item: ReleaseFull.Episode,
    val isEven: Boolean
) : ListItem("${item.releaseId}_${item.id}")

data class ReleaseTorrentListItem(val item: TorrentItem) : ListItem("${item.id}_${item.hash}")
data class ReleaseExpandListItem(val title: String) : ListItem(title)
data class ReleaseEpisodeControlItem(
    val item: ReleaseFull,
    val hasWeb: Boolean,
    val place: EpisodeControlPlace
) : ListItem("${item.id}_$place")

data class ReleaseEpisodesHeadListItem(val tabTag: String) : ListItem(tabTag)
data class ReleaseDonateListItem(val id: Any) : ListItem(id)
data class ReleaseRemindListItem(val item: String) : ListItem(item)
data class ReleaseBlockedListItem(val item: ReleaseFull) : ListItem(item.id)
data class ReleaseHeadListItem(val item: ReleaseFull) : ListItem(item.id)


/* Search screen */

data class SearchListItem(val item: SearchItem) : ListItem(item.id)
data class SearchSuggestionListItem(val item: SuggestionItem) : ListItem(item.id)
data class GenreListItem(val item: GenreItem) : ListItem(item.value)

data class YoutubeListItem(val item: YoutubeItem) : ListItem(item.id)

data class SocialAuthListItem(val item: SocialAuth) : ListItem(item.key)

data class FeedScheduleListItem(val item: ScheduleItem) : ListItem(item.releaseItem.id)
data class FeedSchedulesListItem(val id: Any, val items: List<ScheduleItem>) : ListItem(id)
data class FeedSectionListItem(
    var title: String,
    val route: String? = null,
    val hasBg: Boolean = false,
    val center: Boolean = false
) : ListItem(title)

data class FeedListItem(val item: FeedItem) : ListItem("${item.release?.id}_${item.youtube?.id}")
data class FeedRandomBtnListItem(val id: Any) : ListItem(id)
