package ru.radiationx.anilibria

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.fragment_config.*
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.BaseFragment
import ru.radiationx.shared_app.viewModel
import toothpick.InjectConstructor
import toothpick.ktp.delegate.inject

class ConfigFragment : BaseFragment(R.layout.fragment_config) {

    private val viewModel: CringeViewModel by viewModel()

    private val shit: SomeShit by inject()

    @InjectConstructor
    class CringeViewModel(
        private val apiConfig: ApiConfig,
        private val appBuildConfig: AppBuildConfig,
        private val shit: SomeShit
    ) : ViewModel() {

        init {

            Log.e("lalala", "CringeViewModel inited $this")
            //check()
        }

        fun check() {
            Log.e("lalala", "CringeViewModel with ${apiConfig.active} and ${appBuildConfig.applicationId} and shit=${shit.kek}")

        }

        override fun onCleared() {
            super.onCleared()
            Log.e("lalala", "CringeViewModel onCleared $this")
        }
    }

    @InjectConstructor
    class SomeShit() {
        val kek = "kek $this"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainConstraint.post {
            mainConstraint?.transitionToEnd()
        }
        viewModel.check()
        Log.e("lalala", "main check shit $shit")

        Handler().postDelayed({
            errorState()
        }, 3000)

        configActionRepeat.setOnClickListener {
            normalState()
            Handler().postDelayed({
                errorState()
            }, 3000)
        }
    }

    private fun errorState() {
        TransitionManager.beginDelayedTransition(mainConstraint)
        configErrorGroup.visible()
        configProgressBar.gone()
        configActionRepeat.post {
            configActionRepeat.requestFocus()
        }
    }

    private fun normalState() {
        TransitionManager.beginDelayedTransition(mainConstraint)
        configErrorGroup.gone()
        configProgressBar.visible()
    }


}