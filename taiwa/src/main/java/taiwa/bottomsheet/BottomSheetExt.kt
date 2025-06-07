package taiwa.bottomsheet

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import taiwa.common.DialogType
import taiwa.common.DialogWrapper
import taiwa.common.NestedTaiwa
import taiwa.common.Taiwa
import taiwa.dsl.TaiwaNestingScope
import taiwa.dsl.TaiwaScope
import taiwa.lifecycle.lifecycleLazy

fun Fragment.bottomSheet() = lifecycleLazy {
    DialogWrapper(requireContext(), viewLifecycleOwner, DialogType.BottomSheet)
}

fun ComponentActivity.bottomSheet() = lifecycleLazy {
    DialogWrapper(this, this, DialogType.BottomSheet)
}


fun Fragment.bottomSheetTaiwa(
    block: (TaiwaScope.() -> Unit)? = null,
) = lifecycleLazy {
    Taiwa(requireContext(), viewLifecycleOwner, DialogType.BottomSheet).apply {
        block?.apply(::setContent)
    }
}

fun ComponentActivity.bottomSheetTaiwa(
    block: (TaiwaScope.() -> Unit)? = null,
) = lifecycleLazy {
    Taiwa(this, this, DialogType.BottomSheet).apply {
        block?.apply(::setContent)
    }
}


fun Fragment.nestedBottomSheetTaiwa(
    block: (TaiwaNestingScope.() -> Unit)? = null,
) = lifecycleLazy {
    NestedTaiwa(requireContext(), viewLifecycleOwner, DialogType.BottomSheet).apply {
        block?.apply(::setContent)
    }
}

fun ComponentActivity.nestedBottomSheetTaiwa(
    block: (TaiwaNestingScope.() -> Unit)? = null,
) = lifecycleLazy {
    NestedTaiwa(this, this, DialogType.BottomSheet).apply {
        block?.apply(::setContent)
    }
}

