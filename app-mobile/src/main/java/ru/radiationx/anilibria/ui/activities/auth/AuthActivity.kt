package ru.radiationx.anilibria.ui.activities.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityAuthBinding
import ru.radiationx.anilibria.navigation.BaseFragmentScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.anilibria.utils.initInsets
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.getExtra


/**
 * Created by radiationx on 30.12.17.
 */
class AuthActivity : BaseActivity(R.layout.activity_auth) {

    companion object {
        private const val ARG_INIT_SCREEN = "arg_screen"

        fun newIntent(context: Context, rootScreen: BaseFragmentScreen? = null): Intent =
            Intent(context, AuthActivity::class.java).apply {
                putExtra(ARG_INIT_SCREEN, rootScreen)
            }
    }

    private val binding by viewBinding<ActivityAuthBinding>()

    private val router by inject<Router>()

    private val navigationHolder by inject<NavigatorHolder>()

    private val dimensionsProvider by inject<DimensionsProvider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding.initInsets(dimensionsProvider)

        if (savedInstanceState == null) {
            val initScreen = getExtra<BaseFragmentScreen>(ARG_INIT_SCREEN) ?: Screens.AuthMain()
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
        object : AppNavigator(this, R.id.root_container) {

        }
    }
}
