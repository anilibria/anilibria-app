package ru.radiationx.anilibria.ui.activities.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_container.*
import kotlinx.android.synthetic.main.activity_main.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.navigation.BaseAppScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared_app.di.injectDependencies
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject


/**
 * Created by radiationx on 30.12.17.
 */
class AuthActivity : BaseActivity() {

    companion object {
        private const val ARG_INIT_SCREEN = "arg_screen"

        fun createIntent(context: Context, rootScreen: BaseAppScreen? = null): Intent =
            Intent(context, AuthActivity::class.java).apply {
                putExtra(ARG_INIT_SCREEN, rootScreen)
            }
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var navigationHolder: NavigatorHolder

    @Inject
    lateinit var dimensionsProvider: DimensionsProvider

    private var dimensionHelper: DimensionHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        bottomShadow.gone()
        tabsRecycler.gone()

        dimensionHelper = DimensionHelper(
            measure_view,
            measure_root_content,
            object : DimensionHelper.DimensionsListener {
                override fun onDimensionsChange(dimensions: DimensionHelper.Dimensions) {
                    root_container.post {
                        root_container.setPadding(
                            root_container.paddingLeft,
                            root_container.paddingTop,
                            root_container.paddingRight,
                            dimensions.keyboardHeight
                        )
                    }
                    dimensionsProvider.update(dimensions)
                }
            })

        if (savedInstanceState == null) {
            val initScreen = (intent?.extras?.getSerializable(ARG_INIT_SCREEN) as? BaseAppScreen)
                ?: Screens.AuthMain()
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

    override fun onDestroy() {
        super.onDestroy()
        dimensionHelper?.destroy()
    }

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
