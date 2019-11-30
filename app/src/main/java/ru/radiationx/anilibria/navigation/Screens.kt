package ru.radiationx.anilibria.navigation

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.activities.CountryBlockedActivity
import ru.radiationx.anilibria.ui.activities.SettingsActivity
import ru.radiationx.anilibria.ui.activities.auth.AuthActivity
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.fragments.TabFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthSocialFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthVkFragment
import ru.radiationx.anilibria.ui.fragments.favorites.FavoritesFragment
import ru.radiationx.anilibria.ui.fragments.feed.FeedFragment
import ru.radiationx.anilibria.ui.fragments.history.HistoryFragment
import ru.radiationx.anilibria.ui.fragments.other.OtherFragment
import ru.radiationx.anilibria.ui.fragments.page.PageFragment
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesFragment
import ru.radiationx.anilibria.ui.fragments.schedule.ScheduleFragment
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.ui.fragments.youtube.YoutubeFragment

/**
 * Created by radiationx on 17.11.17.
 */
object Screens {

    class TabScreen(val rootScreen: BaseAppScreen) : BaseAppScreen() {
        override fun getFragment() = TabFragment.newInstance(rootScreen)
    }

    class Auth(val rootScreen: BaseAppScreen? = null) : BaseAppScreen() {
        override fun getActivityIntent(context: Context) = AuthActivity.createIntent(context, rootScreen)
    }

    class AuthMain : BaseAppScreen() {
        override fun getFragment() = AuthFragment()
    }

    class AuthVk(val url: String) : BaseAppScreen() {
        override fun getFragment() = AuthVkFragment.newInstance(url)
    }

    class AuthSocial(val key: String) : BaseAppScreen() {
        override fun getFragment() = AuthSocialFragment.newInstance(key)
    }

    class Main : BaseAppScreen() {
        override fun getActivityIntent(context: Context) = MainActivity.getIntent(context)
    }

    class Settings : BaseAppScreen() {
        override fun getActivityIntent(context: Context) = SettingsActivity.getIntent(context)
    }

    class Favorites : BaseAppScreen() {
        override fun getFragment() = FavoritesFragment()
    }

    class StaticPage(val pagePath: String, val title: String? = null) : BaseAppScreen() {
        override fun getFragment() = PageFragment.newInstance(pagePath)
    }

    class History : BaseAppScreen() {
        override fun getFragment() = HistoryFragment()
    }

    class Schedule(val day: Int = -1) : BaseAppScreen() {
        override fun getFragment(): Fragment = ScheduleFragment.newInstance(day)
    }

    class ReleaseDetails(
            val id: Int = -1,
            val code: String? = null,
            val item: ReleaseItem? = null
    ) : BaseAppScreen() {
        override fun getFragment() = ReleaseFragment.newInstance(id, code, item)
    }

    class ReleasesSearch(
            val genres: String? = null,
            val years: String? = null
    ) : BaseAppScreen() {
        override fun getFragment() = SearchFragment.newInstance(genres, years)
    }

    class MainReleases : BaseAppScreen() {
        override fun getFragment() = ReleasesFragment()
    }

    class MainFeed : BaseAppScreen() {
        override fun getFragment() = FeedFragment()
    }


    class MainYouTube : BaseAppScreen() {
        override fun getFragment() = YoutubeFragment()
    }

    class MainOther : BaseAppScreen() {
        override fun getFragment() = OtherFragment()
    }

    class BlockedCountry : BaseAppScreen() {
        override fun getActivityIntent(context: Context?): Intent = Intent(context, CountryBlockedActivity::class.java)
    }
}
