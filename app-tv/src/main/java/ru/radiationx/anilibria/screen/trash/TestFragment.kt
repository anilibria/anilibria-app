package ru.radiationx.anilibria.screen.trash

import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.test_fragment.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.screen.TestScreen
import ru.radiationx.shared.ktx.android.attachBackPressed
import ru.radiationx.shared_app.screen.BaseFragment
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class TestFragment : BaseFragment(R.layout.test_fragment) {

    @Inject
    lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachBackPressed {
            if (isEnabled) {
                Toast.makeText(requireContext(), "Hello", Toast.LENGTH_SHORT).show()
                isEnabled = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testText.text = "${dependencyInjector.parentScopeTag} > ${dependencyInjector.screenScopeTag}"
        btnback.setOnClickListener {
            router.exit()
        }
        btnfwd.setOnClickListener {
            router.navigateTo(TestScreen())
        }
    }
}