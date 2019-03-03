package ru.radiationx.anilibria.ui.adapters

import ru.radiationx.anilibria.entity.app.auth.SocialAuth
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.ui.activities.main.MainActivity

sealed class ListItem

/* Other screen*/

class ProfileListItem(val profileItem: ProfileItem) : ListItem()
class MenuListItem(val menuItem: OtherMenuItem) : ListItem()
object DividerShadowListItem : ListItem()


/* Common */

object LoadMoreListItem : ListItem()
object CommentRouteListItem : ListItem()
class VitalWebListItem(val item: VitalItem) : ListItem()
class VitalNativeListItem(val item: VitalItem) : ListItem()
class BottomTabListItem(val item: MainActivity.Tab, var selected: Boolean = false) : ListItem()
class PlaceholderListItem(val icRes: Int, val titleRes: Int, val descRes: Int) : ListItem()

/* Releases list screen */

class ReleaseListItem(val item: ReleaseItem) : ListItem()


/* Release detail screen */

class ReleaseEpisodeListItem(val item: ReleaseFull.Episode, val isEven: Boolean) : ListItem()
class ReleaseEpisodeControlItem(val item: ReleaseFull) : ListItem()
class ReleaseEpisodesHeadListItem(val tabTag: String) : ListItem()
object ReleaseDonateListItem : ListItem()
class ReleaseRemindListItem(val item: String) : ListItem()
class ReleaseBlockedListItem(val item: ReleaseFull) : ListItem()
class ReleaseHeadListItem(val item: ReleaseFull) : ListItem()


/* Search screen */

class SearchListItem(val item: SearchItem) : ListItem()
class SearchSuggestionListItem(val item: SuggestionItem) : ListItem()
class GenreListItem(val item: GenreItem) : ListItem()

class YoutubeListItem(val item: YoutubeItem) : ListItem()

class SocialAuthListItem(val item: SocialAuth) : ListItem()

class FeedScheduleListItem(val item: ReleaseItem) : ListItem()
class FeedSchedulesListItem(val items: List<ReleaseItem>) : ListItem()
class FeedSectionListItem(val title: String) : ListItem()
class FeedListItem(val item: FeedItem) : ListItem()
