package ru.radiationx.shared.ktx.android

import android.os.Bundle
import androidx.fragment.app.Fragment

fun <T : Fragment> T.putExtra(block: Bundle.() -> Unit): T {
    val bundle = (arguments ?: Bundle()).apply(block)
    block(bundle)
    arguments = bundle
    return this

}