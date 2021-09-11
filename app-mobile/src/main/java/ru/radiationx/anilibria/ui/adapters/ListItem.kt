package ru.radiationx.anilibria.ui.adapters

import ru.radiationx.anilibria.model.*
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.anilibria.ui.fragments.other.OtherMenuItemState
import ru.radiationx.anilibria.ui.fragments.other.ProfileItemState
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.TorrentItem

sealed class ListItem(private val idData: Any?) {

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

data class ProfileListItem(val id: Any, val state: ProfileItemState) : ListItem(id)
data class MenuListItem(val menuItem: OtherMenuItemState) : ListItem(menuItem.title)
data class DividerShadowListItem(val id: Any) : ListItem(id)


/* Common */

data class LoadMoreListItem(val id: Any) : ListItem(null)
data class LoadErrorListItem(val id: Any) : ListItem(null)
data class CommentRouteListItem(val id: Any) : ListItem(null)
data class BottomTabListItem(
    val item: MainActivity.Tab,
    val selected: Boolean
) : ListItem(item.screen.screenKey)

data class PlaceholderListItem(
    val icRes: Int,
    val titleRes: Int,
    val descRes: Int
) : ListItem(titleRes)

/* Releases list screen */

data class ReleaseListItem(val item: ReleaseItemState) : ListItem(item.id)


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

data class SuggestionLocalListItem(val state: SuggestionLocalItemState) : ListItem(state.id)

data class SuggestionListItem(val state: SuggestionItemState) : ListItem(state.id)

data class YoutubeListItem(val state: YoutubeItemState) : ListItem(state.id)

data class SocialAuthListItem(val state: SocialAuthItemState) : ListItem(state.key)

data class FeedScheduleListItem(val state: ScheduleItemState) : ListItem(state.releaseId)
data class FeedSchedulesListItem(val id: Any, val items: List<ScheduleItemState>) : ListItem(id)
data class FeedSectionListItem(
    val tag: String,
    val title: String,
    val route: String?,
    val hasBg: Boolean = false,
    val center: Boolean = false
) : ListItem(tag)

data class FeedListItem(val item: FeedItemState) :
    ListItem("${item.release?.id}_${item.youtube?.id}")

data class FeedRandomBtnListItem(val id: Any) : ListItem(id)
