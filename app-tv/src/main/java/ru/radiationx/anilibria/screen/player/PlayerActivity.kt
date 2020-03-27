package ru.radiationx.anilibria.screen.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commitNow
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.di.putScopeArgument
import ru.radiationx.shared_app.screen.ScopedFragmentActivity

class PlayerActivity : ScopedFragmentActivity(R.layout.activity_fragments) {

    companion object {
        private const val ARG_ID = "id"

        fun getIntent(context: Context, releaseId: Int): Intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra(ARG_ID, releaseId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val releaseId = intent?.getIntExtra(ARG_ID, -1) ?: -1

        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                val fragment = PlayerFragment.newInstance(releaseId).apply {
                    putScopeArgument(screenScopeTag)
                }
                replace(R.id.fragmentContainer, fragment)
            }

        }
    }
}