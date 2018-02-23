package ru.radiationx.anilibria.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

/**
 * Created by radiationx on 23.02.18.
 */
class IntentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("lalala", "IntentActivity intent: $intent")
        intent?.let {
            it.data?.let {
                startActivity(Intent(this@IntentActivity, MainActivity::class.java).apply {
                    data = it
                    //addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                })
            }
        }
        finish()
    }
}