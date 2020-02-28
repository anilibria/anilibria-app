/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.radiationx.anilibria

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.activity_main.*
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.BaseFragmentActivity
import ru.radiationx.shared_app.viewModel
import toothpick.InjectConstructor
import toothpick.ktp.delegate.inject

/**
 * Loads [MainFragment].
 */
class MainActivity : BaseFragmentActivity() {


    private val viewModel: CringeViewModel by viewModel()
    private val shit by inject<SomeShit>()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

    override fun onResume() {
        super.onResume()
    }
}
