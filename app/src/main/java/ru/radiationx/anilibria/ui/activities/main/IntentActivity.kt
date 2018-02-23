package ru.radiationx.anilibria.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import ru.radiationx.anilibria.App

/**
 * Created by radiationx on 23.02.18.
 */
class IntentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("lalala", "IntentActivity intent: $intent")
        intent?.let {
            it.data?.let {
                val screen = App.injections.linkHandler.findScreen(it.toString())
                Log.e("lalala", "screen: $screen, url=${it.toString()}")
                if (screen != null) {
                    startActivity(Intent(this@IntentActivity, MainActivity::class.java).apply {
                        data = it
                    })
                }
            }
        }
        finish()
    }
}