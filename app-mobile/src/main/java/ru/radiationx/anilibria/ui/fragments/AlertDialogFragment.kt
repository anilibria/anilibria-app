package ru.radiationx.anilibria.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import moxy.MvpAppCompatDialogFragment

open class AlertDialogFragment(@LayoutRes val layoutRes: Int) : MvpAppCompatDialogFragment() {

    private var currentRootView: View? = null

    fun getAlertDialog(): AlertDialog? = (dialog as AlertDialog?)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).create()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onViewCreated(requireView(), savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootLayout = inflater.inflate(layoutRes, container, false)
        currentRootView = rootLayout
        (dialog as AlertDialog).setView(rootLayout)
        return null
    }

    override fun getView(): View? {
        return currentRootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentRootView = null
    }
}