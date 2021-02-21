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
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem

sealed class ListItem

/* Other screen*/

class ProfileListItem(val profileItem: ProfileItem) : ListItem()
class MenuListItem(val menuItem: OtherMenuItem) : ListItem()
class DividerShadowListItem : ListItem()


/* Common */

class LoadMoreListItem : ListItem()
class CommentRouteListItem : ListItem()
class VitalWebListItem(val item: VitalItem) : ListItem()
class VitalNativeListItem(val item: VitalItem) : ListItem()
class BottomTabListItem(val item: MainActivity.Tab, var selected: Boolean = false) : ListItem()
class PlaceholderListItem(val icRes: Int, val titleRes: Int, val descRes: Int) : ListItem()

/* Releases list screen */

class ReleaseListItem(val item: ReleaseItem) : ListItem()


/* Release detail screen */

class ReleaseEpisodeListItem(val item: ReleaseFull.Episode, val isEven: Boolean) : ListItem()
class ReleaseTorrentListItem(val item: TorrentItem) : ListItem()
class ReleaseExpandListItem(val title: String) : ListItem()
class ReleaseEpisodeControlItem(
    val item: ReleaseFull,
    val hasWeb: Boolean,
    val place: EpisodeControlPlace
) : ListItem()

class ReleaseEpisodesHeadListItem(val tabTag: String) : ListItem()
class ReleaseDonateListItem : ListItem()
class ReleaseRemindListItem(val item: String) : ListItem()
class ReleaseBlockedListItem(val item: ReleaseFull) : ListItem()
class ReleaseHeadListItem(val item: ReleaseFull) : ListItem()


/* Search screen */

class SearchListItem(val item: SearchItem) : ListItem()
class SearchSuggestionListItem(val item: SuggestionItem) : ListItem()
class GenreListItem(val item: GenreItem) : ListItem()

class YoutubeListItem(val item: YoutubeItem) : ListItem()

class SocialAuthListItem(val item: SocialAuth) : ListItem()

class FeedScheduleListItem(val item: ScheduleItem) : ListItem()
class FeedSchedulesListItem(val items: List<ScheduleItem>) : ListItem()
class FeedSectionListItem(
    var title: String,
    val route: String? = null,
    val hasBg: Boolean = false,
    val center: Boolean = false
) : ListItem()

class FeedListItem(val item: FeedItem) : ListItem()
class FeedRandomBtnListItem : ListItem()
