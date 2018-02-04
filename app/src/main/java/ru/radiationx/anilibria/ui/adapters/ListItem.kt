package ru.radiationx.anilibria.ui.adapters

import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.other.OtherMenuItem
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem

sealed class ListItem

/* Other screen*/

class ProfileListItem(val profileItem: ProfileItem) : ListItem()
class MenuListItem(val menuItem: OtherMenuItem) : ListItem()
class DividerShadowListItem : ListItem()


/* Common */

class LoadMoreListItem : ListItem()
class CommentListItem(val item: Comment) : ListItem()
class CommentRouteListItem : ListItem()
class VitalWebListItem(val item: VitalItem) : ListItem()
class VitalNativeListItem(val item: VitalItem) : ListItem()

/* Articles, blogs, etc. list screen*/

class ArticleListItem(val item: ArticleItem) : ListItem()


/* Releases list screen */

class ReleaseListItem(val item: ReleaseItem) : ListItem()


/* Release detail screen */

class ReleaseEpisodeListItem(val item: ReleaseFull.Episode) : ListItem()
class ReleaseEpisodesHeadListItem : ListItem()
class ReleaseDonateListItem : ListItem()
class ReleaseRemindListItem : ListItem()
class ReleaseBlockedListItem(val item: ReleaseFull) : ListItem()
class ReleaseHeadListItem(val item: ReleaseFull) : ListItem()


/* Search screen */

class SearchSuggestionListItem(val item: SearchItem) : ListItem()
class GenreListItem(val item: GenreItem) : ListItem()

