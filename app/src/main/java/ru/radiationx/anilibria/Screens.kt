package ru.radiationx.anilibria

import android.content.Context
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.activities.SettingsActivity
import ru.radiationx.anilibria.ui.activities.auth.AuthActivity
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.fragments.TabFragment
import ru.radiationx.anilibria.ui.fragments.article.details.ArticleFragment
import ru.radiationx.anilibria.ui.fragments.article.list.ArticlesContainerFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthVkFragment
import ru.radiationx.anilibria.ui.fragments.favorites.FavoritesFragment
import ru.radiationx.anilibria.ui.fragments.history.HistoryFragment
import ru.radiationx.anilibria.ui.fragments.other.OtherFragment
import ru.radiationx.anilibria.ui.fragments.page.PageFragment
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesFragment
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.ui.fragments.youtube.YoutubeFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen
import java.io.Serializable

/**
 * Created by radiationx on 17.11.17.
 */
class Screens {
    open class AppScreen : SupportAppScreen(), Serializable

    class TabScreen(val rootScreen: AppScreen) : AppScreen() {
        override fun getFragment() = TabFragment.newInstance(rootScreen)
    }

    class Auth(val rootScreen: AppScreen? = null) : AppScreen() {
        override fun getActivityIntent(context: Context) = AuthActivity.createIntent(context, rootScreen)
    }

    // Not working
    class AuthSocial(val extra: Any?) : AppScreen() {
        override fun getFragment() = AuthFragment()
    }

    class AuthVk(val url: String) : AppScreen() {
        override fun getFragment() = AuthVkFragment.newInstance(url)
    }

    class Main : AppScreen() {
        override fun getActivityIntent(context: Context) = MainActivity.getIntent(context)
    }

    class Settings : AppScreen() {
        override fun getActivityIntent(context: Context) = SettingsActivity.getIntent(context)
    }

    class Favorites : AppScreen() {
        override fun getFragment() = FavoritesFragment()
    }

    class StaticPage(val pageId: String) : AppScreen() {
        override fun getFragment() = PageFragment.newInstance(pageId)
    }

    class History : AppScreen() {
        override fun getFragment() = HistoryFragment()
    }

    class ArticleDetails(
            val idCode: String? = null,
            val item: ArticleItem? = null
    ) : AppScreen() {
        override fun getFragment() = ArticleFragment.newInstance(idCode, item)
    }

    class ReleaseDetails(
            val id: Int = -1,
            val code: String? = null,
            val item: ReleaseItem? = null
    ) : AppScreen() {
        override fun getFragment() = ReleaseFragment.newInstance(id, code, item)
    }

    class ReleasesSearch(
            val genres: String? = null,
            val years: String? = null
    ) : AppScreen() {
        override fun getFragment() = SearchFragment.newInstance(genres, years)
    }

    class MainReleases : AppScreen() {
        override fun getFragment() = ReleasesFragment()
    }

    class MainArticles : AppScreen() {
        override fun getFragment() = ArticlesContainerFragment()
    }

    class MainYouTube : AppScreen() {
        override fun getFragment() = YoutubeFragment()
    }

    class MainOther : AppScreen() {
        override fun getFragment() = OtherFragment()
    }
}
