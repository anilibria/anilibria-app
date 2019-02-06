package ru.radiationx.anilibria.ui.activities.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_container.*
import kotlinx.android.synthetic.main.activity_main.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.extension.getMainStyleRes
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.auth.AuthFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthSocialFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthVkFragment
import ru.radiationx.anilibria.ui.navigation.AppNavigator
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.terrakok.cicerone.Navigator
import ru.radiationx.anilibria.ui.navigation.AppRouter


/**
 * Created by radiationx on 30.12.17.
 */
class AuthActivity : AppCompatActivity(), RouterProvider {

    companion object {
        private const val ARG_INIT_SCREEN = "arg_screen"

        fun createIntent(context: Context, rootScreen: Screens.AppScreen? = null): Intent = Intent(context, AuthActivity::class.java).apply {
            putExtra(AuthActivity.ARG_INIT_SCREEN, rootScreen)
        }
    }

    override fun getRouter(): AppRouter = App.navigation.root.router
    override fun getNavigator(): Navigator = navigatorNew
    private val navigationHolder = App.navigation.root.holder
    private val appThemeHolder = App.injections.appThemeHolder

    private val dimensionsProvider = App.injections.dimensionsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(appThemeHolder.getTheme().getMainStyleRes())
        setContentView(R.layout.activity_main)


        bottomShadow.visibility = View.GONE
        tabsRecycler.visibility = View.GONE

        DimensionHelper(measure_view, measure_root_content, object : DimensionHelper.DimensionsListener {
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
            val initScreen = (intent?.extras?.getSerializable(ARG_INIT_SCREEN) as? Screens.AppScreen)
                    ?: Screens.Auth()
            getRouter().newRootScreen(initScreen)
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

    private val navigatorNew by lazy {
        object : AppNavigator(this, R.id.root_container) {

        }
    }
}
