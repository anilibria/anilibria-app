package ru.radiationx.anilibria

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
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
import ru.terrakok.cicerone.Screen
import ru.terrakok.cicerone.android.support.SupportAppScreen
import java.io.Serializable

/**
 * Created by radiationx on 17.11.17.
 */
class Screens {
    open class AppScreen : SupportAppScreen(), Serializable

    class TabScreen(private val rootScreen: AppScreen) : AppScreen() {
        override fun getFragment(): Fragment = TabFragment().apply {
            arguments = Bundle().apply {
                putSerializable(TabFragment.LOCAL_ROOT_SCREEN, rootScreen)
            }
        }
    }

    class Auth(private val screenExtra: Bundle? = null) : AppScreen() {
        override fun getActivityIntent(context: Context?): Intent = Intent(context, AuthActivity::class.java).apply {
            putExtra(AuthActivity.ARG_INIT_SCREEN, screenExtra?.getSerializable(AuthActivity.ARG_INIT_SCREEN))
        }
    }

    class AuthSocial(private val extra: Any?) : AppScreen() {
        override fun getFragment(): Fragment = AuthFragment()
    }

    class AuthVk(private val extra: Any?) : AppScreen() {
        override fun getFragment(): Fragment = AuthVkFragment().apply {
            arguments = Bundle().apply {
                val extra = extra as Bundle?
                extra?.also {
                    putString(AuthVkFragment.ARG_URL, it.getString(AuthVkFragment.ARG_URL))
                }
            }
        }
    }

    class Main : AppScreen() {
        override fun getActivityIntent(context: Context?): Intent = Intent(context, MainActivity::class.java)
    }

    class Settings : AppScreen() {
        override fun getActivityIntent(context: Context?): Intent = Intent(context, SettingsActivity::class.java)
    }

    class Favorites : AppScreen() {
        override fun getFragment(): Fragment = FavoritesFragment()
    }

    class StaticPage(private val data: Any?) : AppScreen() {
        override fun getFragment(): Fragment = PageFragment().apply {
            arguments = Bundle().apply {
                putString(PageFragment.ARG_ID, data as String)
            }
        }
    }

    class History : AppScreen() {
        override fun getFragment(): Fragment = HistoryFragment()
    }

    class ArticleDetails(private val data: Any?) : AppScreen() {
        override fun getFragment(): Fragment = ArticleFragment().apply {
            if (data is Bundle) arguments = data
        }
    }

    class ReleaseDetails(private val data: Any?) : AppScreen() {
        override fun getFragment(): Fragment = ReleaseFragment().apply {
            if (data is Bundle) arguments = data
        }
    }

    class ReleasesSearch(private val data: Any? = null) : AppScreen() {
        override fun getFragment(): Fragment = SearchFragment().apply {
            if (data is Bundle) arguments = data
        }
    }

    class MainReleases : AppScreen() {
        override fun getFragment(): Fragment = ReleasesFragment()
    }

    class MainArticles : AppScreen() {
        override fun getFragment(): Fragment = ArticlesContainerFragment()
    }

    class MainYouTube : AppScreen() {
        override fun getFragment(): Fragment = YoutubeFragment()
    }

    class MainOther : AppScreen() {
        override fun getFragment(): Fragment = OtherFragment()
    }

    companion object {
        const val RELEASE_DETAILS = "RELEASE_DETAILS"
        const val RELEASES_SEARCH = "RELEASES_SEARCH"

        const val MAIN_RELEASES = "MAIN_RELEASES"
        const val MAIN_ARTICLES = "MAIN_ARTICLES"
        const val MAIN_VIDEOS = "MAIN_VIDEOS"
        const val MAIN_BLOGS = "MAIN_BLOGS"
        const val MAIN_OTHER = "MAIN_OTHER"
        const val MAIN_YOUTUBE = "MAIN_YOUTUBE"

        const val ARTICLE_DETAILS = "ARTICLE_DETAILS"

        const val AUTH = "AUTH"
        const val AUTH_SOCIAL = "AUTH_SOCIAL"
        const val AUTH_VK = "AUTH_VK"
        const val MAIN = "MAIN"
        const val SETTINGS = "SETTINGS"

        const val FAVORITES = "FAVORITES"
        const val STATIC_PAGE = "STATIC_PAGE"

        const val HISTORY = "HISTORY"

        const val PLAYER_WEB = "PLAYER_WEB"
    }


}
