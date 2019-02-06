package ru.radiationx.anilibria.extension

import android.os.Bundle
import android.support.v4.app.Fragment

fun <T : Fragment> T.putExtra(block: Bundle.() -> Unit): T {
    val bundle = Bundle().apply(block)
    block(bundle)
    arguments = bundle
    return this

}