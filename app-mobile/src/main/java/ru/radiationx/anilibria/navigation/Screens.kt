package ru.radiationx.anilibria.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import ru.radiationx.anilibria.ui.activities.SettingsActivity
import ru.radiationx.anilibria.ui.activities.WebPlayerActivity
import ru.radiationx.anilibria.ui.activities.auth.AuthActivity
import ru.radiationx.anilibria.ui.activities.main.IntentActivity
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.activities.player.VideoPlayerActivity
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateCheckerActivity
import ru.radiationx.anilibria.ui.fragments.TabFragment
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
import ru.radiationx.data.apinext.models.Genre
import ru.radiationx.data.apinext.models.SocialType
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.FilterType

/**
 * Created by radiationx on 17.11.17.
 */
object Screens {

    class AppUpdateScreen(
        private val force: Boolean,
        private val analyticsFrom: String,
    ) : BaseActivityScreen() {
        override fun createIntent(context: Context): Intent {
            return UpdateCheckerActivity.newIntent(context, force, analyticsFrom)
        }
    }

    class IntentHandler(
        private val uri: String? = null
    ) : BaseActivityScreen() {
        override fun createIntent(context: Context) = IntentActivity.newIntent(context, uri)
    }

    class Main(
        private val url: String? = null
    ) : BaseActivityScreen() {
        override fun createIntent(context: Context) = MainActivity.newIntent(context, url)
    }

    class Settings : BaseActivityScreen() {
        override fun createIntent(context: Context) = SettingsActivity.newIntent(context)
    }

    class Auth(
        private val rootScreen: BaseFragmentScreen? = null
    ) : BaseActivityScreen() {
        override fun createIntent(context: Context) =
            AuthActivity.newIntent(context, rootScreen)
    }

    class Player(
        private val episodeId: EpisodeId,
    ) : BaseActivityScreen() {
        override fun createIntent(context: Context) =
            VideoPlayerActivity.newIntent(context, episodeId)
    }

    class WebPlayer(
        private val link: String,
        private val code: String,
    ) : BaseActivityScreen() {
        override fun createIntent(context: Context) =
            WebPlayerActivity.newIntent(context, link, code)
    }

    class TabScreen(
        private val rootScreen: BaseFragmentScreen
    ) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = TabFragment.newInstance(rootScreen)
    }

    class AuthMain : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = AuthFragment()
    }

    class AuthVk(val url: String) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = AuthVkFragment.newInstance(url)
    }

    class AuthSocial(val type: SocialType) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = AuthSocialFragment.newInstance(type)
    }

    class Favorites : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = SearchCatalogFragment.newInstance(FilterType.Favorites)
    }

    class StaticPage(
        private val pagePath: String,
        val title: String? = null
    ) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = PageFragment.newInstance(pagePath)
    }

    class History(
        private val importUri: Uri? = null
    ) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) =
            HistoryFragment.newInstance(importUri)
    }

    class Schedule(
        val day: Int? = null
    ) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory): Fragment =
            ScheduleFragment.newInstance(day)
    }

    class ReleaseDetails(
        val id: ReleaseId? = null,
        val code: ReleaseCode? = null,
        val item: Release? = null,
    ) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) =
            ReleaseFragment.newInstance(id, code, item)
    }

    class Collections : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) =
            SearchCatalogFragment.newInstance(FilterType.Collections)
    }

    class Catalog(
        private val genre: Genre? = null,
    ) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) =
            SearchCatalogFragment.newInstance(FilterType.Catalog, genre)
    }

    class MainFeed : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = FeedFragment()
    }


    class MainYouTube : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = YoutubeFragment()
    }

    class MainOther : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory) = OtherFragment()
    }

    class DonationDetail : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return DonationDetailFragment()
        }
    }

    class Teams(
        private val query: String? = null
    ) : BaseFragmentScreen() {
        override fun createFragment(factory: FragmentFactory): Fragment {
            return TeamsFragment.newInstance(query)
        }
    }
}
