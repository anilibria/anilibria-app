package ru.radiationx.anilibria.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivitySettingsBinding
import ru.radiationx.anilibria.di.DimensionsModule
import ru.radiationx.anilibria.ui.fragments.settings.SettingsFragment
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import kotlin.math.max


/**
 * Created by radiationx on 25.12.16.
 */

class SettingsActivity : BaseActivity(R.layout.activity_settings) {

    private val binding by viewBinding<ActivitySettingsBinding>()

    private val dimensionsProvider by inject<DimensionsProvider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.PreferencesDayNightAppTheme)
        enableEdgeToEdge()
        installModules(DimensionsModule())
        super.onCreate(savedInstanceState)
        binding.initInsets(dimensionsProvider)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_content, SettingsFragment())
            .commitNow()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return true
    }

    private fun ActivitySettingsBinding.initInsets(dimensionsProvider: DimensionsProvider) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val dimensions = Dimensions(
                statusBar = systemBarInsets.top,
                navigationBar = max(systemBarInsets.bottom, imeInsets.bottom),
            )

            appbarLayout.updatePadding(
                left = systemBarInsets.left,
                top = systemBarInsets.top,
                right = systemBarInsets.right,
            )

            fragmentContent.updatePadding(
                left = systemBarInsets.left,
                right = systemBarInsets.right,
            )
            dimensionsProvider.update(dimensions)
            insets
        }

        root.doOnAttach {
            it.requestApplyInsets()
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
