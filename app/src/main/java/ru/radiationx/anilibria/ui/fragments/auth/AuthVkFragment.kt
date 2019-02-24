package ru.radiationx.anilibria.ui.fragments.auth

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
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.putExtra
import ru.radiationx.anilibria.model.data.holders.AuthHolder
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.terrakok.cicerone.Router
import java.util.regex.Pattern

class AuthVkFragment : BaseFragment() {
    companion object {
        private const val ARG_URL = "ARG_SOCIAL_URL"

        fun newInstance(url: String) = AuthVkFragment().putExtra {
            putString(AuthVkFragment.ARG_URL, url)
        }
    }


    private var startUrl = ""
    private val resultUrlPattern = Pattern.compile("widget\\.html", Pattern.CASE_INSENSITIVE)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            startUrl = it.getString(ARG_URL)
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_webview

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appbarLayout.visibility = View.GONE

        setStatusBarVisibility(true)
        setStatusBarColor(view.context.getColorFromAttr(R.attr.cardBackground))

        webView.settings.apply {
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webView.loadUrl(startUrl)


        webView.webViewClient = object : WebViewClient() {

            var loadingFinished = true
            var redirect = false

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.e("S_DEF_LOG", "OverrideUrlLoading: " + url)
                if (!loadingFinished) {
                    redirect = true
                }

                loadingFinished = false

                val matcher = resultUrlPattern.matcher(url)
                if (matcher.find()) {
                    getDependency(screenScope, AuthHolder::class.java).changeVkAuth(true)
                    //todo
                    //(activity as RouterProvider).getRouter().exitWithResult(RETURN_URL, "")
                    getDependency(screenScope, Router::class.java).exit()
                    return true
                }
                //view.loadUrl(request.url.toString())
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingFinished = false
                progressBar.visibility = View.VISIBLE

                Log.e("S_DEF_LOG", "ON onPageStarted")
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {

                Log.e("S_DEF_LOG", "ON onPageFinished")
                if (!redirect) {
                    loadingFinished = true
                }

                if (loadingFinished && !redirect) {
                    progressBar.visibility = View.GONE
                } else {
                    redirect = false
                }
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                progressBar.visibility = View.GONE
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