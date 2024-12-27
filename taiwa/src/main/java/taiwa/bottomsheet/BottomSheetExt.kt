package taiwa.bottomsheet

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import taiwa.common.DialogType
import taiwa.common.DialogWrapper
import taiwa.common.NestedTaiwa
import taiwa.common.Taiwa
import taiwa.dsl.TaiwaContentScope
import taiwa.dsl.TaiwaRootContentScope

fun Fragment.bottomSheet(): Lazy<DialogWrapper> = lazy {
    DialogWrapper(requireContext(), viewLifecycleOwner, DialogType.BottomSheet)
}

fun ComponentActivity.bottomSheet(): Lazy<DialogWrapper> = lazy {
    DialogWrapper(this, this, DialogType.BottomSheet)
}


fun Fragment.bottomSheetTaiwa(
    block: (TaiwaContentScope.() -> Unit)? = null,
): Lazy<Taiwa> = lazy {
    Taiwa(requireContext(), viewLifecycleOwner, DialogType.BottomSheet).apply {
        block?.apply(::setContent)
    }
}

fun ComponentActivity.bottomSheetTaiwa(
    block: (TaiwaContentScope.() -> Unit)? = null,
): Lazy<Taiwa> = lazy {
    Taiwa(this, this, DialogType.BottomSheet).apply {
        block?.apply(::setContent)
    }
}


fun Fragment.nestedBottomSheetTaiwa(
    block: (TaiwaRootContentScope.() -> Unit)? = null,
): Lazy<NestedTaiwa> = lazy {
    NestedTaiwa(requireContext(), viewLifecycleOwner, DialogType.BottomSheet).apply {
        block?.apply(::setContent)
    }
}

fun ComponentActivity.nestedBottomSheetTaiwa(
    block: (TaiwaRootContentScope.() -> Unit)? = null,
): Lazy<NestedTaiwa> = lazy {
    NestedTaiwa(this, this, DialogType.BottomSheet).apply {
        block?.apply(::setContent)
    }
}

