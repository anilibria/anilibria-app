package ru.radiationx.anilibria.ui.activities.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityMainBinding
import ru.radiationx.anilibria.navigation.BaseAppScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.anilibria.utils.initInsets
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.getExtra
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator


/**
 * Created by radiationx on 30.12.17.
 */
class AuthActivity : BaseActivity(R.layout.activity_main) {

    companion object {
        private const val ARG_INIT_SCREEN = "arg_screen"

        fun createIntent(context: Context, rootScreen: BaseAppScreen? = null): Intent =
            Intent(context, AuthActivity::class.java).apply {
                putExtra(ARG_INIT_SCREEN, rootScreen)
            }
    }

    private val binding by viewBinding<ActivityMainBinding>()

    private val router by inject<Router>()

    private val navigationHolder by inject<NavigatorHolder>()

    private val dimensionsProvider by inject<DimensionsProvider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding.bottomShadow.isGone = true
        binding.tabsRecycler.isGone = true

        binding.initInsets(dimensionsProvider)

        if (savedInstanceState == null) {
            val initScreen = getExtra<BaseAppScreen>(ARG_INIT_SCREEN) ?: Screens.AuthMain()
            router.newRootScreen(initScreen)
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigatorNew)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.root_container)
        val fragmentBackHandled = (fragment as? BackButtonListener)?.onBackPressed() ?: false
        val canPopScreen = supportFragmentManager.backStackEntryCount >= 1
        val handleResult = when {
            fragmentBackHandled -> true
            canPopScreen -> {
                router.exit()
                true
            }

            else -> false
        }
        if (!handleResult) {
            super.onBackPressed()
        }
    }

    private val navigatorNew by lazy {
        object : SupportAppNavigator(this, R.id.root_container) {

        }
    }
}
