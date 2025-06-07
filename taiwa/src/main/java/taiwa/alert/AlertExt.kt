package taiwa.alert

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import taiwa.common.DialogType
import taiwa.common.DialogWrapper
import taiwa.common.NestedTaiwa
import taiwa.common.Taiwa
import taiwa.dsl.TaiwaNestingScope
import taiwa.dsl.TaiwaScope
import taiwa.lifecycle.lifecycleLazy

fun Fragment.alert() = lifecycleLazy {
    DialogWrapper(requireContext(), viewLifecycleOwner, DialogType.Alert)
}

fun ComponentActivity.alert() = lifecycleLazy {
    DialogWrapper(this, this, DialogType.Alert)
}


fun Fragment.alertTaiwa(
    block: (TaiwaScope.() -> Unit)? = null,
) = lifecycleLazy {
    Taiwa(requireContext(), viewLifecycleOwner, DialogType.Alert).apply {
        block?.apply(::setContent)
    }
}

fun ComponentActivity.alertTaiwa(
    block: (TaiwaScope.() -> Unit)? = null,
) = lifecycleLazy {
    Taiwa(this, this, DialogType.Alert).apply {
        block?.apply(::setContent)
    }
}


fun Fragment.nestedAlertTaiwa(
    block: (TaiwaNestingScope.() -> Unit)? = null,
) = lifecycleLazy {
    NestedTaiwa(requireContext(), viewLifecycleOwner, DialogType.Alert).apply {
        block?.apply(::setContent)
    }
}

fun ComponentActivity.nestedAlertTaiwa(
    block: (TaiwaNestingScope.() -> Unit)? = null,
) = lifecycleLazy {
    NestedTaiwa(this, this, DialogType.Alert).apply {
        block?.apply(::setContent)
    }
}