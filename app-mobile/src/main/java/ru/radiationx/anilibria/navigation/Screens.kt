package ru.radiationx.anilibria.navigation

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import ru.radiationx.anilibria.ui.activities.CountryBlockedActivity
import ru.radiationx.anilibria.ui.activities.SettingsActivity
import ru.radiationx.anilibria.ui.activities.auth.AuthActivity
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateCheckerActivity
import ru.radiationx.anilibria.ui.fragments.TabFragment
import ru.radiationx.anilibria.ui.fragments.auth.main.Auth2FaCodeFragment
import ru.radiationx.anilibria.ui.fragments.auth.main.AuthFragment
import ru.radiationx.anilibria.ui.fragments.auth.social.AuthSocialFragment
import ru.radiationx.anilibria.ui.fragments.auth.vk.AuthVkFragment
import ru.radiationx.anilibria.ui.fragments.donation.detail.DonationDetailFragment
import ru.radiationx.anilibria.ui.fragments.favorites.FavoritesFragment
import ru.radiationx.anilibria.ui.fragments.feed.FeedFragment
import ru.radiationx.anilibria.ui.fragments.history.HistoryFragment
import ru.radiationx.anilibria.ui.fragments.other.OtherFragment
import ru.radiationx.anilibria.ui.fragments.page.PageFragment
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.ui.fragments.schedule.ScheduleFragment
import ru.radiationx.anilibria.ui.fragments.search.SearchCatalogFragment
import ru.radiationx.anilibria.ui.fragments.teams.TeamsFragment
import ru.radiationx.anilibria.ui.fragments.youtube.YoutubeFragment
import ru.radiationx.data.entity.app.release.ReleaseItem

/**
 * Created by radiationx on 17.11.17.
 */
object Screens {

    class AppUpdateScreen(val force: Boolean, val analyticsFrom: String) : BaseAppScreen() {
        override fun getActivityIntent(context: Context): Intent {
            return UpdateCheckerActivity.newIntent(context, force, analyticsFrom)
        }
    }

    class TabScreen(val rootScreen: BaseAppScreen) : BaseAppScreen() {
        override fun getFragment() = TabFragment.newInstance(rootScreen)
    }

    class Auth(val rootScreen: BaseAppScreen? = null) : BaseAppScreen() {
        override fun getActivityIntent(context: Context) =
            AuthActivity.createIntent(context, rootScreen)
    }

    class AuthMain : BaseAppScreen() {
        override fun getFragment() = AuthFragment()
    }

    class Auth2FaCode(val login: String, val password: String) : BaseAppScreen() {
        override fun getFragment() = Auth2FaCodeFragment.newInstance(login, password)
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
        override fun getFragment(): androidx.fragment.app.Fragment =
            ScheduleFragment.newInstance(day)
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
        override fun getFragment() = SearchCatalogFragment.newInstance(genres, years)
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
        override fun getActivityIntent(context: Context?): Intent =
            Intent(context, CountryBlockedActivity::class.java)
    }

    class DonationDetail : BaseAppScreen() {
        override fun getFragment(): Fragment {
            return DonationDetailFragment()
        }
    }

    class Teams : BaseAppScreen() {
        override fun getFragment(): Fragment {
            return TeamsFragment()
        }
    }
}
