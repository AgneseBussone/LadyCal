package com.beacat.calendar.ladycal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Splash screen shown the minimum amount of time needed to start the app.
 * No fixed time requested, I don't want to waste user's time.
 */

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No need to setup a view, because it comes from the theme

        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)

        // when the onCreate on the Main Activity ends and it became visible,
        // this activity will be closed
        finish()
    }
}
