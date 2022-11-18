package ru.radiationx.anilibria.screen.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.leanback.app.BrowseSupportFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.databinding.FragmentProfileBinding
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModelFromParent
import ru.radiationx.shared_app.imageloader.showImageUrl
import ru.radiationx.shared_app.screen.ScopedFragment
import javax.inject.Inject

class ProfileFragment : ScopedFragment(R.layout.fragment_profile),
    BrowseSupportFragment.MainFragmentAdapterProvider {

    private val binding by viewBinding<FragmentProfileBinding>()

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    private val viewModel by viewModelFromParent<ProfileViewModel>()

    private val selfMainFragmentAdapter by lazy { BrowseSupportFragment.MainFragmentAdapter(this) }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return selfMainFragmentAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeTo(viewModel.profileData) {
            if (!it.avatarUrl.isNullOrEmpty()) {
                binding.profileAvatar.showImageUrl(it.avatarUrl)
            }
            binding.profileNick.text = it.nick

            val auth = it.authState == AuthState.AUTH
            binding.profileAvatar.isVisible = auth
            binding.profileNick.isVisible = auth
            binding.profileSignIn.isGone = auth
            binding.profileSignOut.isVisible = auth
        }

        binding.profileSignIn.setOnClickListener { viewModel.onSignInClick() }
        binding.profileSignOut.setOnClickListener { viewModel.onSignOutClick() }


        mainFragmentAdapter.fragmentHost.notifyViewCreated(selfMainFragmentAdapter)
        mainFragmentAdapter.fragmentHost.notifyDataReady(selfMainFragmentAdapter)
        backgroundManager.clearGradient()
    }
}