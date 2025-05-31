package ru.radiationx.anilibria.screen.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseSupportFragment
import androidx.lifecycle.lifecycleScope
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.databinding.FragmentProfileBinding
import ru.radiationx.quill.inject
import ru.radiationx.shared_app.di.quillParentViewModel
import ru.radiationx.shared_app.imageloader.showImageUrl

class ProfileFragment : Fragment(R.layout.fragment_profile),
    BrowseSupportFragment.MainFragmentAdapterProvider {

    private val binding by viewBinding<FragmentProfileBinding>()

    private val backgroundManager by inject<GradientBackgroundManager>()

    private val viewModel by quillParentViewModel<ProfileViewModel>()

    private val selfMainFragmentAdapter by lazy { BrowseSupportFragment.MainFragmentAdapter(this) }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return selfMainFragmentAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        viewModel.profileData.onEach {
            if (!it?.avatar.isNullOrEmpty()) {
                binding.profileAvatar.showImageUrl(it?.avatar)
            }
            binding.profileNick.text = it?.nickname

            val hasAuth = it != null
            binding.profileAvatar.isVisible = hasAuth
            binding.profileNick.isVisible = hasAuth
            binding.profileSignIn.isGone = hasAuth
            binding.profileSignOut.isVisible = hasAuth
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.profileSignIn.setOnClickListener { viewModel.onSignInClick() }
        binding.profileSignOut.setOnClickListener { viewModel.onSignOutClick() }


        mainFragmentAdapter.fragmentHost.notifyViewCreated(selfMainFragmentAdapter)
        mainFragmentAdapter.fragmentHost.notifyDataReady(selfMainFragmentAdapter)
        backgroundManager.clearGradient()
    }
}