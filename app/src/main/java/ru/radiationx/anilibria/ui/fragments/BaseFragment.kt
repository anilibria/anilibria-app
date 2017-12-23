package ru.radiationx.anilibria.ui.fragments;

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatFragment
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.common.BackButtonListener

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment : MvpAppCompatFragment(), BackButtonListener {
    abstract val layoutRes: Int

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater.inflate(R.layout.fragment_main_base, container, false)
        inflater.inflate(layoutRes, newView?.findViewById(R.id.fragment_content), true)
        return newView
    }
}
