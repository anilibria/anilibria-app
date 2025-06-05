package ru.radiationx.anilibria.ui.fragments.release.loader

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ru.radiationx.anilibria.R
import ru.radiationx.data.common.ReleaseCode
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra

class ReleaseLoaderFragment : Fragment(R.layout.fragment_release_loader) {

    companion object {
        private const val ARG_CODE = "release_code"

        fun newInstance(
            code: ReleaseCode? = null,
        ) = ReleaseLoaderFragment().putExtra {
            putParcelable(ARG_CODE, code)
        }
    }

    private val viewModel by viewModel<ReleaseLoaderViewModel> {
        ReleaseLoaderExtra(
            code = getExtraNotNull(ARG_CODE)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadRelease()
    }
}