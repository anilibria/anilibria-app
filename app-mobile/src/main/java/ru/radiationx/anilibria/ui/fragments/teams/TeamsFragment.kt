package ru.radiationx.anilibria.ui.fragments.teams

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import kotlinx.android.synthetic.main.fragment_teams.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.teams.TeamsPresenter
import ru.radiationx.anilibria.presentation.teams.TeamsView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.teams.adapter.TeamsAdapter
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.data.entity.domain.team.Teams

class TeamsFragment : BaseFragment(), TeamsView {

    @InjectPresenter
    lateinit var presenter: TeamsPresenter

    @ProvidePresenter
    fun providePresenter(): TeamsPresenter = getDependency(TeamsPresenter::class.java, screenScope)

    private val contentAdapter = TeamsAdapter()

    override fun getBaseLayout(): Int = R.layout.fragment_teams

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = AutoTransition()
        exitTransition = AutoTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamsToolbar.setNavigationOnClickListener { presenter.onBackPressed() }

        rvTeams.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)
            disableItemChangeAnimation()
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        teamsToolbar.updatePadding(top = dimensions.statusBar)
        rvTeams.updatePadding(bottom = dimensions.navigationBar)
    }

    override fun showData(data: Teams) {
        contentAdapter.bindState(data)
    }

    override fun setLoading(isLoading: Boolean) {
        pbLoading.isVisible = isLoading
    }

}