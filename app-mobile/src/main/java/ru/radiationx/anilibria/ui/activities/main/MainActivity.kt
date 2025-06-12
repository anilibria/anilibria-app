package ru.radiationx.anilibria.ui.activities.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ads.BannerAdController
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.databinding.ActivityMainBinding
import ru.radiationx.anilibria.di.DimensionsModule
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.activities.updatechecker.CheckerExtra
import ru.radiationx.anilibria.ui.activities.updatechecker.CheckerViewModel
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateNotificationHelper
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.IntentHandler
import ru.radiationx.anilibria.ui.common.NetworkStatusBinder
import ru.radiationx.anilibria.ui.fragments.TabResetter
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.utils.dimensions.Dimensions
import ru.radiationx.anilibria.utils.dimensions.DimensionsProvider
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.features.ActivityLaunchAnalytics
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.isLaunchedFromHistory
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared_app.networkstatus.NetworkStatusViewModel

class MainActivity : BaseActivity(R.layout.activity_main) {

    companion object {
        private const val TABS_STACK = "TABS_STACK"

        fun newIntent(context: Context, url: String? = null) =
            Intent(context, MainActivity::class.java).apply {
                data = url?.let { Uri.parse(it) }
            }
    }

    private val appThemeController by inject<AppThemeController>()

    private val screenMessenger by inject<SystemMessenger>()

    private val router by inject<Router>()

    private val navigationHolder by inject<NavigatorHolder>()

    private val dimensionsProvider by inject<DimensionsProvider>()

    private val permissionsController by inject<MintPermissionsController>()

    private val binding by viewBinding<ActivityMainBinding>()


    private val viewModel by viewModel<MainViewModel>()
    private val networkStatusViewModel by viewModel<NetworkStatusViewModel>()

    private val checkerViewModel by viewModel<CheckerViewModel> {
        CheckerExtra(forceLoad = true)
    }

    private val bannerAdController by lazy {
        BannerAdController(this, binding.bannerAdBview, binding.bannerAdContainer)
    }

    private val tabsViewModel by viewModel<MainTabsViewModel>()

    private val navigator by lazy {
        AppNavigator(this, R.id.root_container)
    }
    private val tabsFragmentManager by lazy {
        MainTabsFragmentManager(this, R.id.tabs_container)
    }

    private val tabsAdapter by lazy {
        BottomTabsAdapter(
            clickListener = { tabsViewModel.onTabClick(it) },
            longClickListener = { tabsViewModel.onTabLongClick(it) }
        )
    }

    private var createdWithSavedState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        enableEdgeToEdge()
        installModules(DimensionsModule())
        super.onCreate(savedInstanceState)
        if (isLaunchedFromHistory()) {
            get<ActivityLaunchAnalytics>().launchFromHistory(this, savedInstanceState)
        }
        createdWithSavedState = savedInstanceState != null

        binding.initInsets(dimensionsProvider)

        viewModel.init()
        tabsViewModel.init(savedInstanceState?.getExtra(TABS_STACK))

        binding.tabsRecycler.apply {
            layoutManager = GridLayoutManager(this.context, 5)
            adapter = tabsAdapter
            itemAnimator = null
        }

        tabsViewModel.tabsState.onEach {
            tabsFragmentManager.setState(it.tabs, it.selected)
            (binding.tabsRecycler.layoutManager as GridLayoutManager).spanCount = it.tabs.size
            tabsAdapter.bindItems(it)
        }.launchInResumed(this)

        tabsViewModel.scrollTopEvent.onEach {
            val fragment = supportFragmentManager.findFragmentByTag(it.key)
            if (fragment is TopScroller) {
                fragment.scrollToTop()
            }
        }.launchInResumed(this)

        tabsViewModel.tabResetEvent.onEach {
            val fragment = supportFragmentManager.findFragmentByTag(it.key)
            if (fragment is TabResetter) {
                fragment.resetTab()
            }
        }.launchInResumed(this)

        checkerViewModel.state.mapNotNull { it.data }.onEach {
            UpdateNotificationHelper.showUpdateData(this, it, permissionsController)
        }.launchInResumed(this)

        viewModel.state.map { it.mainLogicCompleted }.filter { it }.distinctUntilChanged().onEach {
            onMainLogicCompleted()
        }.launchInResumed(this)

        viewModel.state.mapNotNull { it.adsConfig }.distinctUntilChanged().onEach {
            bannerAdController.load(
                it.mainBanner,
                appThemeController.getTheme()
            )
        }.launchInResumed(this)

        networkStatusViewModel.state.onEach {
            NetworkStatusBinder.bind(
                transitionRoot = binding.activityRoot,
                statusWrapper = binding.networkStatusWrapper,
                statusView = binding.networkStatus,
                state = it
            )
        }.launchInResumed(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigationHolder.removeNavigator()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(TABS_STACK, ArrayList(tabsViewModel.tabsState.value.stack))
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.tabsRecycler.adapter = null
        bannerAdController.destroy()
    }

    private fun onMainLogicCompleted() {
        if (!createdWithSavedState) {
            handleIntent(intent)
        }
        checkerViewModel.checkUpdate()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val fragment =
            supportFragmentManager.findFragmentByTag(tabsViewModel.tabsState.value.selected.key)
        val check = fragment != null
                && fragment is BackButtonListener
                && (fragment as BackButtonListener).onBackPressed()
        if (check) {
            return
        } else {
            tabsViewModel.onBackPressed()
        }
    }

    private fun ActivityMainBinding.initInsets(provider: DimensionsProvider) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBarInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val containerInsetList = listOf(
                systemBarInsets.bottom,
                appFooter.height,
                imeInsets.bottom
            )
            val containerInsetsBottom = containerInsetList.max()

            val dimensions = Dimensions(
                left = systemBarInsets.left,
                top = systemBarInsets.top,
                right = systemBarInsets.right,
            )
            layoutActivityContainer.root.updatePadding(
                bottom = containerInsetsBottom
            )
            appFooter.updatePadding(
                left = systemBarInsets.left,
                right = systemBarInsets.right,
                bottom = systemBarInsets.bottom
            )
            networkStatusWrapper.updatePadding(
                left = systemBarInsets.left,
                right = systemBarInsets.right,
            )
            provider.update(dimensions)
            insets
        }

        root.doOnAttach {
            it.requestApplyInsets()
        }

        appFooter.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            root.requestApplyInsets()
        }
        appFooter.doOnLayout {
            root.requestApplyInsets()
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.also { intentData ->
            val url = intentData.toString()
            val handled =
                findTabIntentHandler(url, tabsViewModel.tabsState.value.stack.asReversed())
            if (!handled) {
                findTabIntentHandler(url, tabsViewModel.tabsState.value.stack)
            }
        }
        intent?.data = null
    }

    private fun findTabIntentHandler(url: String, tabs: List<MainTab>): Boolean {
        val fm = supportFragmentManager
        tabs.forEach { tab ->
            fm.findFragmentByTag(tab.key)?.let {
                if (it is IntentHandler && it.handle(url)) {
                    tabsViewModel.onTabClick(tab)
                    return true
                }
            }
        }
        return false
    }
}
