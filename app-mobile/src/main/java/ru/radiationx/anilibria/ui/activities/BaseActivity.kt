package ru.radiationx.anilibria.ui.activities

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import ru.radiationx.anilibria.ui.common.ScreenMessagesObserver
import ru.radiationx.quill.inject

open class BaseActivity(
    @LayoutRes contentLayoutId: Int = 0
) : AppCompatActivity(contentLayoutId) {

    private val screenMessagesObserver by inject<ScreenMessagesObserver>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(screenMessagesObserver)
    }
}