package ru.radiationx.anilibria.ui.activities.main

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityMainBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.navigation.BaseAppScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.activities.updatechecker.CheckerExtra
import ru.radiationx.anilibria.ui.activities.updatechecker.CheckerViewModel
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateDataState
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.IntentHandler
import ru.radiationx.anilibria.ui.fragments.configuring.ConfiguringFragment
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.anilibria.utils.initInsets
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.ActivityLaunchAnalytics
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.android.immutableFlag
import ru.radiationx.shared.ktx.android.isLaunchedFromHistory
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace


class MainActivity : BaseActivity(R.layout.activity_main) {

    companion object {
        private const val TABS_STACK = "TABS_STACK"
        private const val SELECTED_TAB = "SELECTED_TAB"

        fun newIntent(context: Context, url: String? = null) =
            Intent(context, MainActivity::class.java).apply {
                data = url?.let { Uri.parse(it) }
            }
    }

    private val screenMessenger by inject<SystemMessenger>()

    private val router by inject<Router>()

    private val navigationHolder by inject<NavigatorHolder>()

    private val dimensionsProvider by inject<DimensionsProvider>()

    private val binding by viewBinding<ActivityMainBinding>()

    private val tabsAdapter by lazy { BottomTabsAdapter(tabsListener) }

    private val allTabs = arrayOf(
        Tab(R.string.fragment_title_releases, R.drawable.ic_newspaper, Screens.MainFeed()),
        Tab(R.string.fragment_title_favorites, R.drawable.ic_star, Screens.Favorites()),
        Tab(R.string.fragment_title_search, R.drawable.ic_toolbar_search, Screens.Catalog()),
        Tab(R.string.fragment_title_youtube, R.drawable.ic_youtube, Screens.MainYouTube()),
        Tab(R.string.fragment_title_other, R.drawable.ic_other, Screens.MainOther())
    )
    private val tabs = mutableListOf<Tab>()

    private val tabsStack = mutableListOf<String>()

    private val viewModel by viewModel<MainViewModel>()

    private val checkerViewModel by viewModel<CheckerViewModel> {
        CheckerExtra(forceLoad = true)
    }

    private var createdWithSavedState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        if (isLaunchedFromHistory()) {
            get<ActivityLaunchAnalytics>().launchFromHistory(this, savedInstanceState)
        }
        createdWithSavedState = savedInstanceState != null

        viewModel.init(savedInstanceState?.getString(SELECTED_TAB))
        savedInstanceState?.getStringArrayList(TABS_STACK)?.also {
            if (it.isNotEmpty()) {
                tabsStack.addAll(it)
            }
        }

        binding.initInsets(dimensionsProvider)

        binding.tabsRecycler.apply {
            layoutManager = GridLayoutManager(this.context, allTabs.size)
            adapter = tabsAdapter
            disableItemChangeAnimation()
        }

        updateTabs()
        initContainers()

        checkerViewModel.state.mapNotNull { it.data }.onEach {
            showUpdateData(it)
        }.launchIn(lifecycleScope)

        viewModel.state.mapNotNull { it.selectedTab }.distinctUntilChanged().onEach {
            highlightTab(it)
        }.launchIn(lifecycleScope)

        // lifecycle needs for fragment manager
        viewModel.state
            .map { it.needConfig }
            .distinctUntilChanged()
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach {
                if (it) {
                    showConfiguring()
                } else {
                    hideConfiguring()
                }
            }
            .launchIn(lifecycleScope)

        viewModel.state.map { it.mainLogicCompleted }.filter { it }.distinctUntilChanged().onEach {
            onMainLogicCompleted()
        }.launchIn(lifecycleScope)

        viewModel.updateTabsAction.observe().onEach {
            updateTabs()
        }.launchInResumed(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.tabsRecycler.adapter = null
    }


    private fun showUpdateData(update: UpdateDataState) {
        if (update.hasUpdate) {
            val channelId = "anilibria_channel_updates"
            val channelName = "Обновления"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }

            val mBuilder = NotificationCompat.Builder(this, channelId)

            val mNotificationManager = NotificationManagerCompat.from(this)

            mBuilder.setSmallIcon(R.drawable.ic_notify)
            mBuilder.color = getCompatColor(R.color.alib_red)

            mBuilder.setContentTitle("Обновление AniLibria")
            mBuilder.setContentText("Новая версия: ${update.name}")
            mBuilder.setChannelId(channelId)


            val notifyIntent =
                Screens.AppUpdateScreen(false, AnalyticsConstants.notification_local_update)
                    .getActivityIntent(this)
            val notifyPendingIntent =
                PendingIntent.getActivity(this, 0, notifyIntent, immutableFlag())
            mBuilder.setContentIntent(notifyPendingIntent)

            mBuilder.setAutoCancel(true)

            mBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
            mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT)

            var defaults = 0
            defaults = defaults or NotificationCompat.DEFAULT_SOUND
            defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
            mBuilder.setDefaults(defaults)

            mNotificationManager.notify(update.code, mBuilder.build())
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigatorNew)
    }

    private fun onMainLogicCompleted() {
        if (!createdWithSavedState) {
            handleIntent(intent)
        }
        checkerViewModel.checkUpdate()
    }

    private fun showConfiguring() {
        binding.configuringContainer.isVisible = true
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.configuring_container, ConfiguringFragment())
            .commitNow()
    }

    private fun hideConfiguring() {
        binding.configuringContainer.isGone = true
        supportFragmentManager.findFragmentById(R.id.configuring_container)?.also {
            supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commitNow()
        }
    }

    override fun onPause() {
        navigationHolder.removeNavigator()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(TABS_STACK, ArrayList(tabsStack))
        outState.putString(SELECTED_TAB, viewModel.state.value.selectedTab)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(tabsStack.lastOrNull())
        val check = fragment != null
                && fragment is BackButtonListener
                && (fragment as BackButtonListener).onBackPressed()
        if (check) {
            return
        } else {
            viewModel.onBackPressed()
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.also { intentData ->
            val url = intentData.toString()
            val handled = findTabIntentHandler(url, tabsStack.asReversed())
            if (!handled) {
                findTabIntentHandler(url, tabs.map { it.screen.screenKey })
            }
        }
        intent?.data = null
    }

    private fun findTabIntentHandler(url: String, tabs: List<String>): Boolean {
        val fm = supportFragmentManager
        tabs.forEach { tab ->
            fm.findFragmentByTag(tab)?.let {
                if (it is IntentHandler && it.handle(url)) {
                    return true
                }
            }
        }
        return false
    }

    private fun initContainers() {
        val fm = supportFragmentManager
        val ta = fm.beginTransaction()
        allTabs.forEach { tab ->
            var fragment: Fragment? = fm.findFragmentByTag(tab.screen.screenKey)
            if (fragment == null) {
                fragment = Screens.TabScreen(tab.screen).fragment
                ta.add(R.id.root_container, fragment, tab.screen.screenKey)
                if (tabsStack.contains(tab.screen.screenKey)) {
                    ta.attach(fragment)
                } else {
                    ta.detach(fragment)
                }
            }
        }
        ta.commitNow()
    }

    private fun updateBottomTabs() {
        tabsAdapter.bindItems(tabs)
        (binding.tabsRecycler.layoutManager as GridLayoutManager).spanCount = tabs.size
    }

    private fun updateTabs() {
        tabs.clear()
        if (viewModel.getAuthState() == AuthState.AUTH) {
            tabs.addAll(allTabs)
        } else {
            tabs.addAll(allTabs.filter { it.screen !is Screens.Favorites })
            removeFromStack(allTabs.first { it.screen is Screens.Favorites }.screen.screenKey)
        }
        updateBottomTabs()
    }

    private fun highlightTab(screenKey: String) {
        tabsAdapter.setSelected(screenKey)
        val screen = tabs.first { it.screen.screenKey == screenKey }.screen
        viewModel.submitScreenAnalytics(screen)
        router.replaceScreen(screen)
    }

    fun addInStack(screenKey: String) {
        tabsStack.remove(screenKey)
        tabsStack.add(screenKey)
    }

    fun removeFromStack(screenKey: String) {
        tabsStack.remove(screenKey)
    }

    private val tabsListener = object : BottomTabsAdapter.Listener {
        override fun onTabClick(tab: Tab) {
            viewModel.selectTab(tab.screen.screenKey)
        }
    }

    private val navigatorNew = object : SupportAppNavigator(this, R.id.root_container) {

        override fun applyCommand(command: Command) {
            if (command is Back) {
                if (tabsStack.size <= 1) {
                    activityBack()
                    return
                }
                val fm = supportFragmentManager
                val ta = fm.beginTransaction()
                val fragment = fm.findFragmentByTag(tabsStack.last())
                fragment?.also { ta.detach(it) }
                removeFromStack(tabsStack.last())
                ta.commitNow()
                if (tabsStack.isNotEmpty()) {
                    viewModel.selectTab(tabsStack.last())
                } else {
                    activityBack()
                }
                return
            } else if (command is Replace) {
                val inTabs =
                    allTabs.firstOrNull { it.screen.screenKey == command.screen.screenKey } != null
                if (inTabs) {
                    val fm = supportFragmentManager
                    val ta = fm.beginTransaction()
                    allTabs.forEach {
                        val fragment = fm.findFragmentByTag(it.screen.screenKey)
                        if (fragment != null) {
                            if (it.screen.screenKey == command.screen.screenKey) {
                                if (fragment.isDetached) {
                                    ta.attach(fragment)
                                }
                                ta.show(fragment)
                                addInStack(it.screen.screenKey)
                            } else {
                                ta.hide(fragment)
                            }
                        }
                    }
                    ta.commitNow()
                    return
                }
            }

            super.applyCommand(command)
        }

        private var exitToastShowed: Boolean = false
        override fun activityBack() {
            if (!exitToastShowed) {
                screenMessenger.showMessage("Нажмите кнопку назад снова, чтобы выйти из программы")
                exitToastShowed = true
                Handler(Looper.getMainLooper()).postDelayed({ exitToastShowed = false }, 3L * 1000)
            } else {
                super.activityBack()
            }
        }
    }

    data class Tab(
        val title: Int,
        val icon: Int,
        val screen: BaseAppScreen,
    )
}
