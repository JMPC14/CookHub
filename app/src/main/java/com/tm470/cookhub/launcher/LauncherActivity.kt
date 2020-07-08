package com.tm470.cookhub.launcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tm470.cookhub.R

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        supportActionBar?.hide()
    }
}