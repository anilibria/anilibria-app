package ru.radiationx.anilibria.ui.fragments.other

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import kotlinx.android.synthetic.main.fragment_test.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import android.webkit.WebSettings
import android.os.Build
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.ui.fragments.article.ArticleAdapter


/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : BaseFragment() {
    override val layoutRes: Int = R.layout.fragment_releases

    val adapter = ArticleAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            setHasFixedSize(true)
            adapter = this@OtherFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }
        adapter.notifyDataSetChanged()
    }

    @JavascriptInterface
    fun resize(value: String) {
        Log.e("SUKA", "Webview resize " + value)
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}
