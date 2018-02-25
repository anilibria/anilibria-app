package ru.radiationx.anilibria.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.activity_container.*
import kotlinx.android.synthetic.main.activity_main.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.presentation.main.MainPresenter
import ru.radiationx.anilibria.presentation.main.MainView
import ru.radiationx.anilibria.ui.activities.auth.AuthActivity
import ru.radiationx.anilibria.ui.activities.updatechecker.SimpleUpdateChecker
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.IntentHandler
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.TabFragment
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.*


class MainActivity : MvpAppCompatActivity(), MainView, RouterProvider, BottomTabsAdapter.Listener {

    companion object {
        private const val TABS_STACK = "TABS_STACK"
    }

    override val router: Router = App.navigation.root.router
    private val navigationHolder = App.navigation.root.holder

    private val tabsAdapter = BottomTabsAdapter(this)

    private val allTabs = arrayOf(
            Tab(R.string.fragment_title_releases, R.drawable.ic_releases, Screens.MAIN_RELEASES),
            Tab(R.string.fragment_title_videos, R.drawable.ic_star, Screens.FAVORITES),
            Tab(R.string.fragment_title_blogs, R.drawable.ic_toolbar_search, Screens.RELEASES_SEARCH),
            Tab(R.string.fragment_title_news, R.drawable.ic_news, Screens.MAIN_ARTICLES),
            Tab(R.string.fragment_title_other, R.drawable.ic_other, Screens.MAIN_OTHER)
    )
    private val tabs = mutableListOf<Tab>()

    private val tabsStack = mutableListOf<String>()

    private val dimensionsProvider = App.injections.dimensionsProvider

    @InjectPresenter
    lateinit var presenter: MainPresenter

    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter {
        return MainPresenter(router, App.injections.authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DimensionHelper(view_for_measure, root_content, object : DimensionHelper.DimensionsListener {
            override fun onDimensionsChange(dimensions: DimensionHelper.Dimensions) {
                view_for_measure.post {
                    activity_root.setPadding(
                            activity_root.paddingLeft,
                            activity_root.paddingTop,
                            activity_root.paddingRight,
                            dimensions.keyboardHeight/* - tabsRecycler.height*/
                    )
                }
                dimensionsProvider.update(dimensions)
            }
        })

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
        if (savedInstanceState == null) {
            SimpleUpdateChecker(App.injections.checkerRepository).checkUpdate()
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
        Log.e("lalala", "MainActivity, onCreate {savedInstanceState == null}, $intent")
        handleIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putStringArrayList(TABS_STACK, ArrayList(tabsStack))
    }

    override fun onBackPressed() {
        val fragment = if (tabsStack.isEmpty()) {
            null
        } else {
            supportFragmentManager.findFragmentByTag(tabsStack.last())
        }
        if (fragment != null
                && fragment is BackButtonListener
                && (fragment as BackButtonListener).onBackPressed()) {
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
                handled = findTabIntentHandler(url, tabs.map { it.screenKey })
            }
            Log.e("lalala", "MainActivity, handled $handled")
        }
        intent?.data = null
    }

    private fun findTabIntentHandler(url: String, tabs: List<String>): Boolean {
        val fm = supportFragmentManager
        tabs.forEach {
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
            var fragment: Fragment? = fm.findFragmentByTag(tab.screenKey)
            if (fragment == null) {
                fragment = TabFragment.newInstance(tab.screenKey)
                ta.add(R.id.root_container, fragment, tab.screenKey)
                if (tabsStack.contains(tab.screenKey)) {
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
        tabs.clear()
        if (presenter.getAuthState() == AuthState.AUTH) {
            tabs.addAll(allTabs)
        } else {
            tabs.addAll(allTabs.filter { it.screenKey != Screens.FAVORITES })
        }
        updateBottomTabs()
    }

    override fun onTabClick(tab: Tab) {
        presenter.selectTab(tab.screenKey)
    }

    override fun highlightTab(screenKey: String) {
        tabsAdapter.setSelected(screenKey)
    }

    fun addInStack(screenKey: String) {
        tabsStack.remove(screenKey)
        tabsStack.add(screenKey)
    }

    fun removeFromStack(screenKey: String) {
        tabsStack.remove(screenKey)
    }

    private val navigatorNew = object : SupportAppNavigator(this, R.id.root_container) {
        override fun createActivityIntent(screenKey: String?, data: Any?): Intent? {
            Log.e("S_DEF_LOG", "Create intent " + screenKey)
            return when (screenKey) {
                Screens.AUTH -> {
                    Log.e("S_DEF_LOG", "REAL CREATE INTENT " + screenKey)
                    Intent(this@MainActivity, AuthActivity::class.java)
                }
                else -> null
            }
        }

        override fun createFragment(screenKey: String?, data: Any?): Fragment? {
            Log.e("S_DEF_LOG", "Create fragment " + screenKey)
            return null
        }

        override fun applyCommand(command: Command) {
            Log.e("S_DEF_LOG", "ApplyCommand " + command)
            if (command is Back) {
                if (tabsStack.size <= 1) {
                    exit()
                    return
                }
                val fm = supportFragmentManager
                val ta = fm.beginTransaction()
                val fragment = fm.findFragmentByTag(tabsStack.last())
                ta.detach(fragment)
                removeFromStack(tabsStack.last())
                ta.commitNow()
                if (tabsStack.isNotEmpty()) {
                    presenter.selectTab(tabsStack.last())
                } else {
                    exit()
                }
                return
            } else if (command is SystemMessage) {
                Toast.makeText(this@MainActivity, command.message, Toast.LENGTH_SHORT).show()
                return
            } else if (command is Replace) {
                val inTabs = allTabs.firstOrNull { it.screenKey == command.screenKey } != null
                if (inTabs) {
                    Log.e("S_DEF_LOG", "Replace " + command.screenKey)
                    val fm = supportFragmentManager
                    val ta = fm.beginTransaction()
                    allTabs.forEach {
                        val fragment = fm.findFragmentByTag(it.screenKey)
                        if (it.screenKey == command.screenKey) {
                            if (fragment.isDetached) {
                                ta.attach(fragment)
                            }
                            ta.show(fragment)
                            addInStack(it.screenKey)
                            Log.e("S_DEF_LOG", "QUEUE: " + tabsStack.joinToString(", ", "[", "]"))
                        } else {
                            ta.hide(fragment)
                        }
                    }
                    ta.commitNow()
                    return
                }
            }

            Log.e("S_DEF_LOG", "sector clear")
            super.applyCommand(command)
        }

        override fun unknownScreen(command: Command?) {
            val screenKey = when {
                command is BackTo -> command.screenKey
                command is Forward -> command.screenKey
                command is Replace -> command.screenKey
                else -> "NO_KEY"
            }
            throw RuntimeException("Can't create a screen for passed screenKey $command, $screenKey")
        }


        private var exitToastShowed: Boolean = false
        override fun exit() {
            if (!exitToastShowed) {
                showSystemMessage("Нажмите кнопку назад снова, чтобы выйти из программы")
                exitToastShowed = true
                Handler().postDelayed({ exitToastShowed = false }, 3L * 1000)
            } else {
                super.exit()
            }
        }
    }

    class Tab(
            val title: Int,
            val icon: Int,
            val screenKey: String
    )
}
