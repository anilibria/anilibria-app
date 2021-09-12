package ru.radiationx.anilibria.ui.fragments.auth.vk

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_webview.*
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.di.injectDependencies
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.visible
import ru.terrakok.cicerone.Router
import java.util.regex.Pattern

class AuthVkFragment : BaseFragment() {
    companion object {
        private const val ARG_URL = "ARG_SOCIAL_URL"

        fun newInstance(url: String) = AuthVkFragment().putExtra {
            putString(ARG_URL, url)
        }
    }


    private var startUrl = ""
    private val resultUrlPattern = Pattern.compile("widget\\.html", Pattern.CASE_INSENSITIVE)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            startUrl = it.getString(ARG_URL, startUrl)
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_webview

    override val statusBarVisible: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appbarLayout.gone()

        webView.settings.apply {
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webView.loadUrl(startUrl)


        webView.webViewClient = object : WebViewClient() {

            var loadingFinished = true
            var redirect = false

            @Suppress("OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.e("S_DEF_LOG", "OverrideUrlLoading: $url")
                if (!loadingFinished) {
                    redirect = true
                }

                loadingFinished = false

                val matcher = resultUrlPattern.matcher(url)
                if (matcher.find()) {
                    getDependency(AuthHolder::class.java, screenScope).changeVkAuth(true)
                    //todo
                    //(activity as RouterProvider).getRouter().exitWithResult(RETURN_URL, "")
                    getDependency(Router::class.java, screenScope).exit()
                    return true
                }
                //view.loadUrl(request.url.toString())
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingFinished = false
                progressBarWv.visible()

                Log.e("S_DEF_LOG", "ON onPageStarted")
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {

                Log.e("S_DEF_LOG", "ON onPageFinished")
                if (!redirect) {
                    loadingFinished = true
                }

                if (loadingFinished && !redirect) {
                    progressBarWv.gone()
                } else {
                    redirect = false
                }
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                progressBarWv.gone()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroyView() {
        webView.webViewClient = null
        webView.stopLoading()
        super.onDestroyView()
    }
}