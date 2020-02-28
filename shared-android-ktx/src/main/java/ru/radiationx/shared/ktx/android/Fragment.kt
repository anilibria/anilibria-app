package ru.radiationx.shared.ktx.android

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

fun <T : Fragment> T.putExtra(block: Bundle.() -> Unit): T {
    val bundle = (arguments ?: Bundle()).apply(block)
    block(bundle)
    arguments = bundle
    return this
}

fun <T : Fragment> T.attachBackPressed(enabled: Boolean = true, block: () -> Unit): OnBackPressedCallback {
    val callback: OnBackPressedCallback = object : OnBackPressedCallback(enabled) {
        override fun handleOnBackPressed() {
            block.invoke()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    return callback
}