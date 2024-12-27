package taiwa.alert

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import taiwa.common.DialogType
import taiwa.common.DialogWrapper
import taiwa.common.NestedTaiwa
import taiwa.common.Taiwa
import taiwa.dsl.TaiwaContentScope
import taiwa.dsl.TaiwaRootContentScope

fun Fragment.alert(): Lazy<DialogWrapper> = lazy {
    DialogWrapper(requireContext(), viewLifecycleOwner, DialogType.Alert)
}

fun ComponentActivity.alert(): Lazy<DialogWrapper> = lazy {
    DialogWrapper(this, this, DialogType.Alert)
}


fun Fragment.alertTaiwa(
    block: (TaiwaContentScope.() -> Unit)? = null,
): Lazy<Taiwa> = lazy {
    Taiwa(requireContext(), viewLifecycleOwner, DialogType.Alert).apply {
        block?.apply(::setContent)
    }
}

fun ComponentActivity.alertTaiwa(
    block: (TaiwaContentScope.() -> Unit)? = null,
): Lazy<Taiwa> = lazy {
    Taiwa(this, this, DialogType.Alert).apply {
        block?.apply(::setContent)
    }
}


fun Fragment.nestedAlertTaiwa(
    block: (TaiwaRootContentScope.() -> Unit)? = null,
): Lazy<NestedTaiwa> = lazy {
    NestedTaiwa(requireContext(), viewLifecycleOwner, DialogType.Alert).apply {
        block?.apply(::setContent)
    }
}

fun ComponentActivity.nestedAlertTaiwa(
    block: (TaiwaRootContentScope.() -> Unit)? = null,
): Lazy<NestedTaiwa> = lazy {
    NestedTaiwa(this, this, DialogType.Alert).apply {
        block?.apply(::setContent)
    }
}