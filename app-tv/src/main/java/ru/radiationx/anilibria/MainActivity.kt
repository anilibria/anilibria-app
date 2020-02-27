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
import androidx.fragment.app.FragmentActivity
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.activity_main.*
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainConstraint.post {
            mainConstraint?.transitionToEnd()
        }

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
