package com.beyondtechnicallycorrect.visitordetector.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.fragments.WelcomeFragment
import timber.log.Timber

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        setContentView(R.layout.activity_welcome)

        if (savedInstanceState != null) {
            return
        }
        val welcomeFragment = WelcomeFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, welcomeFragment)
            .commit()
    }
}
