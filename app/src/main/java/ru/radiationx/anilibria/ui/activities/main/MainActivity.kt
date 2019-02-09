package ru.radiationx.anilibria.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_container.*
import kotlinx.android.synthetic.main.activity_main.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.extension.getMainStyleRes
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.model.repository.CheckerRepository
import ru.radiationx.anilibria.navigation.BaseAppScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.main.MainPresenter
import ru.radiationx.anilibria.presentation.main.MainView
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.activities.updatechecker.SimpleUpdateChecker
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.IntentHandler
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace
import java.util.*
import javax.inject.Inject
import kotlin.math.max


class MainActivity : BaseActivity(), MainView {

    companion object {
        private const val TABS_STACK = "TABS_STACK"

        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var navigationHolder: NavigatorHolder

    @Inject
    lateinit var dimensionsProvider: DimensionsProvider

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val tabsAdapter by lazy { BottomTabsAdapter(tabsListener) }

    private val allTabs = arrayOf(
            Tab(R.string.fragment_title_releases, R.drawable.ic_releases, Screens.MainReleases()),
            Tab(R.string.fragment_title_favorites, R.drawable.ic_star, Screens.Favorites()),
            Tab(R.string.fragment_title_search, R.drawable.ic_toolbar_search, Screens.ReleasesSearch()),
            Tab(R.string.fragment_title_youtube, R.drawable.ic_youtube, Screens.MainYouTube()),
            Tab(R.string.fragment_title_other, R.drawable.ic_other, Screens.MainOther())
    )
    private val tabs = mutableListOf<Tab>()

    private val tabsStack = mutableListOf<String>()

    private lateinit var currentAppTheme: AppThemeHolder.AppTheme

    @InjectPresenter
    lateinit var presenter: MainPresenter

    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter = getDependency(MainPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        currentAppTheme = appThemeHolder.getTheme()
        setTheme(currentAppTheme.getMainStyleRes())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DimensionHelper(measure_view, measure_root_content, object : DimensionHelper.DimensionsListener {
            override fun onDimensionsChange(dimensions: DimensionHelper.Dimensions) {
                Log.e("lalala", "Dim: $dimensions")
                root_container.post {
                    root_container.setPadding(
                            root_container.paddingLeft,
                            root_container.paddingTop,
                            root_container.paddingRight,
                            max(dimensions.keyboardHeight - tabsRecycler.height, 0)
                    )
                }
                dimensionsProvider.update(dimensions)
            }
        })

        antiddos_skip?.setOnClickListener {
            presenter.skipAntiDdos()
        }

        tabsRecycler.apply {
            layoutManager = GridLayoutManager(this.context, allTabs.size)
            adapter = tabsAdapter
        }

        updateTabs()
        initContainers()

        savedInstanceState?.let {
            it.getStringArrayList(TABS_STACK)?.let {
                if (it.isNotEmpty()) {
                    tabsStack.addAll(it)
                    presenter.defaultScreen = it.last()
                }
            }
        }
        Log.e("S_DEF_LOG", "main oncreate")
    }

    override fun setAntiDdosVisibility(isVisible: Boolean) {
        Log.e("MainPresenter", "setAntiDdosVisibility: $isVisible")
        antiDdosMain.visibility = if (isVisible) View.VISIBLE else View.GONE
        if (!isVisible) {
            SimpleUpdateChecker(getDependency(CheckerRepository::class.java)).checkUpdate()
        }
    }

    override fun changeTheme(appTheme: AppThemeHolder.AppTheme) {
        if (currentAppTheme != appTheme) {
            currentAppTheme = appTheme
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                Handler().post { recreate() }
            } else {
                recreate()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("lalala", "MainActivity, onNewIntent $intent")
        handleIntent(intent)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigatorNew)
        /*Log.e("lalala", "MainActivity, onResumeFragments $intent")
        handleIntent(intent)*/
    }

    override fun onMainLogicCompleted() {
        Log.e("lalala", "MainActivity, onMainLogicCompleted $intent")
        handleIntent(intent)
    }

    override fun onPause() {
        navigationHolder.removeNavigator()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putStringArrayList(TABS_STACK, ArrayList(tabsStack))
    }

    override fun onDestroy() {
        super.onDestroy()
        ImageLoader.getInstance().clearMemoryCache()
        ImageLoader.getInstance().stop()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(tabsStack.lastOrNull())
        val check = fragment != null
                && fragment is BackButtonListener
                && (fragment as BackButtonListener).onBackPressed()
        if (check) {
            return
        } else {
            presenter.onBackPressed()
        }
    }

    private fun handleIntent(intent: Intent?) {
        Log.e("lalala", "MainActivity, handleIntent $intent")
        if (intent != null && intent.data != null) {
            val url = intent.data.toString()
            var handled = findTabIntentHandler(url, tabsStack.asReversed())
            if (!handled) {
                handled = findTabIntentHandler(url, tabs.map { it.screen.screenKey })
            }
            Log.e("lalala", "MainActivity, handled $handled")
        }
        intent?.data = null
    }

    private fun findTabIntentHandler(url: String, tabs: List<String>): Boolean {
        val fm = supportFragmentManager
        tabs.forEach {
            Log.e("lalala", "findTabIntentHandler screen $it")
            fm.findFragmentByTag(it)?.let {
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
        (tabsRecycler.layoutManager as GridLayoutManager).spanCount = tabs.size
    }

    override fun updateTabs() {
        Log.e("MainPresenter", "updateTabs")
        tabs.clear()
        if (presenter.getAuthState() == AuthState.AUTH) {
            tabs.addAll(allTabs)
        } else {
            tabs.addAll(allTabs.filter { it.screen !is Screens.Favorites })
        }
        updateBottomTabs()
    }

    override fun highlightTab(screenKey: String) {
        Log.e("MainPresenter", "highlightTab $screenKey")
        tabsAdapter.setSelected(screenKey)
        router.replaceScreen(tabs.first { it.screen.screenKey == screenKey }.screen)
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
            presenter.selectTab(tab.screen.screenKey)
        }
    }

    private val navigatorNew = object : SupportAppNavigator(this, R.id.root_container) {

        override fun applyCommand(command: Command?) {
            Log.e("S_DEF_LOG", "ApplyCommand " + command)
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
                    presenter.selectTab(tabsStack.last())
                } else {
                    activityBack()
                }
                return
            } else if (command is Replace) {
                val inTabs = allTabs.firstOrNull { it.screen.screenKey == command.screen.screenKey } != null
                if (inTabs) {
                    Log.e("S_DEF_LOG", "Replace " + command.screen.screenKey)
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
                                Log.e("S_DEF_LOG", "QUEUE: " + tabsStack.joinToString(", ", "[", "]"))
                            } else {
                                ta.hide(fragment)
                            }
                        }
                    }
                    ta.commitNow()
                    return
                }
            }

            Log.e("S_DEF_LOG", "sector clear")
            super.applyCommand(command)
        }

        private var exitToastShowed: Boolean = false
        override fun activityBack() {
            if (!exitToastShowed) {
                screenMessenger.showMessage("Нажмите кнопку назад снова, чтобы выйти из программы")
                exitToastShowed = true
                Handler().postDelayed({ exitToastShowed = false }, 3L * 1000)
            } else {
                super.activityBack()
            }
        }
    }

    class Tab(
            val title: Int,
            val icon: Int,
            val screen: BaseAppScreen
    )
}
