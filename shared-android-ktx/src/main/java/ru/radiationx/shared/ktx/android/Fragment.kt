package ru.radiationx.shared.ktx.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

/* Intent */
@Suppress("DEPRECATION")
inline fun <reified T : Any> Intent.getExtraNotNull(key: String, default: T? = null): T {
    val value = extras?.get(key)
    return requireNotNull(if (value is T) value else default) { key }
}

@Suppress("DEPRECATION")
inline fun <reified T : Any> Intent.getExtra(key: String, default: T? = null): T? {
    val value = extras?.get(key)
    return if (value is T) value else default
}

/* Activity */
@Suppress("DEPRECATION")
inline fun <reified T : Any> Activity.getExtraNotNull(key: String, default: T? = null): T {
    val value = intent?.extras?.get(key)
    return requireNotNull(if (value is T) value else default) { key }
}

@Suppress("DEPRECATION")
inline fun <reified T : Any> Activity.getExtra(key: String, default: T? = null): T? {
    val value = intent?.extras?.get(key)
    return if (value is T) value else default
}

/* Fragment */
@Suppress("DEPRECATION")
inline fun <reified T : Any> Fragment.getExtra(key: String, default: T? = null): T? {
    val value = arguments?.get(key)
    return if (value is T) value else default
}

@Suppress("DEPRECATION")
inline fun <reified T : Any> Fragment.getExtraNotNull(key: String, default: T? = null): T {
    val value = arguments?.get(key)
    return requireNotNull(if (value is T) value else default) { key }
}


fun <T : Fragment> T.putExtra(block: Bundle.() -> Unit): T {
    val bundle = (arguments ?: Bundle()).apply(block)
    block(bundle)
    arguments = bundle
    return this
}

fun <T : Fragment> T.attachBackPressed(
    enabled: Boolean = true,
    block: OnBackPressedCallback.() -> Unit,
): OnBackPressedCallback {
    val callback: OnBackPressedCallback = object : OnBackPressedCallback(enabled) {
        override fun handleOnBackPressed() {
            block.invoke(this)
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    return callback
}