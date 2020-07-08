package com.tm470.cookhub.launcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tm470.cookhub.R
import com.tm470.cookhub.hideFragment
import com.tm470.cookhub.showFragment
import kotlinx.android.synthetic.main.activity_launcher.*

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val registerFragment = supportFragmentManager.findFragmentById(R.id.register_fragment)!!
        val loginFragment = supportFragmentManager.findFragmentById(R.id.login_fragment)!!

        hideFragment(this, registerFragment)
        hideFragment(this, loginFragment)

        supportActionBar?.hide()

        buttonLogin.setOnClickListener {
            showFragment(this, loginFragment)
        }

        buttonRegister.setOnClickListener {
            showFragment(this, registerFragment)
        }
    }

    override fun onBackPressed() {
        val registerFragment = supportFragmentManager.findFragmentById(R.id.register_fragment)!!
        val loginFragment = supportFragmentManager.findFragmentById(R.id.login_fragment)!!
        if (registerFragment.isVisible) {
            hideFragment(this, registerFragment)
        } else if (loginFragment.isVisible) {
            hideFragment(this, loginFragment)
        } else {
            super.onBackPressed()
        }
    }
}