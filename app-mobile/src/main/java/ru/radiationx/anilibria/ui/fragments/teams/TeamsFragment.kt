package ru.radiationx.anilibria.ui.fragments.teams

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentTeamsBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.teams.adapter.TeamsAdapter
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.putExtra

class TeamsFragment : BaseDimensionsFragment(R.layout.fragment_teams), TopScroller {

    companion object {
        private const val ARG_QUERY = "arg_query"

        fun newInstance(query: String?) = TeamsFragment().putExtra {
            putString(ARG_QUERY, query)
        }
    }

    private val argQuery by lazy { requireArguments().getString(ARG_QUERY) }

    private val contentAdapter = TeamsAdapter {
        viewModel.onHeaderActionClick()
    }

    private val binding by viewBinding<FragmentTeamsBinding>()

    private val viewModel by viewModel<TeamsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = AutoTransition()
        exitTransition = AutoTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.teamsToolbar.setNavigationOnClickListener { viewModel.onBackPressed() }

        binding.rvTeams.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)
            disableItemChangeAnimation()
        }

        binding.btSearchClear.setOnClickListener { binding.etSearch.text?.clear() }
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setQueryText(text?.toString().orEmpty())
            binding.btSearchClear.isVisible = text?.isNotEmpty() == true
        }
        binding.etSearch.setText(argQuery)

        viewModel.state.onEach { state ->
            state.data?.also { contentAdapter.bindState(it) }
            binding.pbLoading.isVisible = state.loading
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        binding.root.updatePadding(
            left = dimensions.left,
            top = dimensions.top,
            right = dimensions.right
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvTeams.adapter = null
    }

    override fun scrollToTop() {
        binding.rvTeams.scrollToPosition(0)
    }

}