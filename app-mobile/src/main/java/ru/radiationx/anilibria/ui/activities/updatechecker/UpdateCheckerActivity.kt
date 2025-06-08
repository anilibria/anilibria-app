package ru.radiationx.anilibria.ui.activities.updatechecker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import androidx.activity.enableEdgeToEdge
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityUpdaterBinding
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.activities.updatechecker.adapter.UpdateContentAdapter
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.ActivityLaunchAnalytics
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.isLaunchedFromHistory
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared.ktx.android.startMainActivity
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils

class UpdateCheckerActivity : BaseActivity(R.layout.activity_updater) {

    companion object {
        private const val ARG_FORCE = "force"
        private const val ARG_ANALYTICS_FROM = "from"

        fun newIntent(context: Context, force: Boolean, analyticsFrom: String) =
            Intent(context, UpdateCheckerActivity::class.java).apply {
                putExtra(ARG_FORCE, force)
                putExtra(ARG_ANALYTICS_FROM, analyticsFrom)
                action = Intent.ACTION_VIEW
            }
    }

    private val binding by viewBinding<ActivityUpdaterBinding>()

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(viewModel::submitUseTime)
    }

    private val viewModel by viewModel<CheckerViewModel> {
        CheckerExtra(forceLoad = getExtraNotNull(ARG_FORCE))
    }

    private val updaterAnalytics by inject<UpdaterAnalytics>()

    private val sharedBuildConfig by inject<SharedBuildConfig>()

    private val systemUtils by inject<SystemUtils>()

    private val contentAdapter = UpdateContentAdapter(
        actionClickListener = { viewModel.onLinkClick(it) },
        cancelClickListener = { viewModel.onCancelDownloadClick(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (isLaunchedFromHistory()) {
            get<ActivityLaunchAnalytics>().launchFromHistory(this, savedInstanceState)
            startMainActivity()
            finish()
            return
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBarInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            binding.root.updatePadding(
                top = systemBarInsets.top,
                left = systemBarInsets.left,
                right = systemBarInsets.right,
            )
            binding.updateRecycler.updatePadding(
                bottom = systemBarInsets.bottom
            )
            binding.updatePlaceholder.updatePadding(
                bottom = systemBarInsets.bottom
            )
            insets
        }
        lifecycle.addObserver(useTimeCounter)
        viewModel.checkUpdate()

        intent?.getStringExtra(ARG_ANALYTICS_FROM)?.also {
            updaterAnalytics.open(it)
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.updateRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
            adapter = contentAdapter
        }

        binding.updatePlaceholderAction.setOnClickListener {
            viewModel.checkUpdate(true)
        }

        viewModel.state.onEach { state ->
            bindState(state)
        }.launchIn(lifecycleScope)

        viewModel.openDownloadedFileAction.observe().onEach {
            systemUtils.open(it)
        }.launchInResumed(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isLaunchedFromHistory()) {
            binding.updateRecycler.adapter = null
        }
    }

    private suspend fun bindState(state: CheckerScreenState) {
        binding.updatePlaceholder.isVisible =
            (state.data == null || !state.data.hasUpdate) && !state.loading
        binding.updatePlaceholderTitle.isVisible = state.data?.hasUpdate != true
        binding.updateRecycler.isVisible = state.data?.hasUpdate == true
        binding.updateLoading.isVisible = state.loading
        binding.updateHeader.isVisible = state.data?.hasUpdate == true
        state.data?.let {
            binding.updateInfo.text = generateInfo(it.name, it.date)
            contentAdapter.bindState(it)
        }
    }

    private fun generateInfo(name: String?, date: String?): Spanned = buildSpannedString {
        bold { append("Версия: ") }
        append(name)
        appendLine()
        bold { append("Сборка от: ") }
        append(date)
    }
}
