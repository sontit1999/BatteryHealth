package com.procharger.fastprocharrging.quickcharge.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ActionHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent?.extras?.getString(com.procharger.fastprocharrging.quickcharge.common.Constants.KEY_ACTION)

        // Open MainActivity
        Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(com.procharger.fastprocharrging.quickcharge.common.Constants.KEY_ACTION, action)
        }.run {
            startActivity(this)
        }

        // Close immediately
        finish()
    }
}