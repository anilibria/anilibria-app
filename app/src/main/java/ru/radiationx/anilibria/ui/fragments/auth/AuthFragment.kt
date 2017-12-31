package ru.radiationx.anilibria.ui.fragments.auth

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.presentation.auth.AuthPresenter
import ru.radiationx.anilibria.presentation.auth.AuthView
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment

/**
 * Created by radiationx on 30.12.17.
 */
class AuthFragment : BaseFragment(), AuthView {

    @InjectPresenter
    lateinit var presenter: AuthPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): AuthPresenter {
        return AuthPresenter((activity as RouterProvider).router, App.injections.authRepository)
    }

    override fun getLayoutResource(): Int = R.layout.fragment_auth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appbarLayout.visibility = View.GONE
        authSubmit.setOnClickListener {
            presenter.signIn(authLogin.text.toString(), authPassword.text.toString())
        }
        authSkip.setOnClickListener {
            presenter.skip()
        }
        ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.ic_launcher, auth_logo)
        //view.setBackgroundColor(Color.BLUE)

        authPatreon.isEnabled = false
        authVk.isEnabled = false
        authPatreon.setOnClickListener {
            (activity as RouterProvider).router.navigateTo("patreon", patreonUrl)
            (activity as RouterProvider).router.setResultListener(1488, {
                val redirect = it as String
                presenter.signIn(redirect)
                Log.e("SUKA", "AUTH PATREON RESULT "+redirect)
                (activity as RouterProvider).router.removeResultListener(1488)
            })
        }

        authVk.setOnClickListener {
            (activity as RouterProvider).router.navigateTo("patreon", vkUrl)
            (activity as RouterProvider).router.setResultListener(1488, {
                val redirect = it as String
                presenter.signIn(redirect)
                Log.e("SUKA", "AUTH PATREON RESULT "+redirect)
                (activity as RouterProvider).router.removeResultListener(1488)
            })
        }
    }

    var patreonUrl: String = ""

    var vkUrl = ""

    override fun setPatreon(patr: String) {
        Log.e("SUKA", "set patreon "+patr)
        authPatreon.isEnabled = true
        patreonUrl = patr
    }

    override fun setVk(vk: String) {
        Log.e("SUKA", "set vk "+vk)
        authVk.isEnabled = true
        vkUrl = vk
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return false
    }

    override fun setRefreshing(refreshing: Boolean) {
        authSwitcher.displayedChild = if (refreshing) 1 else 0
    }
}