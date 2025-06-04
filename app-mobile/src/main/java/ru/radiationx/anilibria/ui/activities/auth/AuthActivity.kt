package ru.radiationx.anilibria.ui.activities.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityAuthBinding
import ru.radiationx.anilibria.di.DimensionsModule
import ru.radiationx.anilibria.navigation.BaseFragmentScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.NetworkStatusBinder
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared_app.networkstatus.NetworkStatusViewModel


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

    private val networkStatusViewModel by viewModel<NetworkStatusViewModel>()

    private val navigatorNew by lazy {
        object : AppNavigator(this, R.id.root_container) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        enableEdgeToEdge()
        installModules(DimensionsModule())
        super.onCreate(savedInstanceState)

        binding.initInsets()

        if (savedInstanceState == null) {
            val initScreen = getExtra<BaseFragmentScreen>(ARG_INIT_SCREEN) ?: Screens.AuthMain()
            router.newRootScreen(initScreen)
        }

        networkStatusViewModel.state.onEach {
            NetworkStatusBinder.bind(
                transitionRoot = binding.activityRoot,
                statusWrapper = binding.networkStatusWrapper,
                statusView = binding.networkStatus,
                state = it
            )
        }.launchInResumed(this)
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

    private fun ActivityAuthBinding.initInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val contentInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                    .or(WindowInsetsCompat.Type.displayCutout())
                    .or(WindowInsetsCompat.Type.ime())
            )
            layoutActivityContainer.root.updatePadding(
                top = contentInsets.top,
                left = contentInsets.left,
                right = contentInsets.right,
                bottom = contentInsets.bottom
            )
            networkStatusWrapper.updatePadding(
                left = contentInsets.left,
                right = contentInsets.right,
                bottom = contentInsets.bottom
            )
            insets
        }

        root.doOnAttach {
            it.requestApplyInsets()
        }
    }
}
